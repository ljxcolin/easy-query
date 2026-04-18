package com.easyquery.core.rewriter.impl;

import com.easyquery.core.enums.ConditionType;
import com.easyquery.core.parser.SqlParseResult;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.rewriter.SqlRewriteResult;
import com.easyquery.core.rewriter.SqlRewriter;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.sharding.ShardingStrategyFactory;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.SqlCondition;
import com.easyquery.core.utils.TableUtils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperation;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.UnionOp;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SelectSqlRewriter implements SqlRewriter {

    private static final Logger logger = LoggerFactory.getLogger(SelectSqlRewriter.class);

    private final SqlParser sqlParser;

    public SelectSqlRewriter(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
    }

    /**
     * 重写 SQL 语句
     * 
     * 处理流程：
     * 1. 解析 SQL，拆分成各个部分
     * 2. 局部重写各部分（FROM、JOIN、WHERE 等）
     * 3. 组装成完整的 SQL
     * 
     * @param entry       数据源条目
     * @param parseResult SQL 解析结果
     * @return 重写后的 SQL 语句
     */
    @Override
    public String rewrite(DataSourceEntry entry, SqlParseResult parseResult) throws JSQLParserException {
        if (entry == null || CollectionUtils.isEmpty(entry.getShardingRuleConfigs())) {
            logger.debug("数据源无可用分片策略，直接返回原SQL");
            return parseResult.getOriginalSql();
        }
        Statement statement = parseResult.getStatement();
        Select select = (Select) statement;
        SelectBody selectBody = select.getSelectBody();
        // 重写 标准的 Select 语句
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            FromItem fromItem = plainSelect.getFromItem();
            // 重写 FROM 表
            if (fromItem instanceof Table) {
                Table fromTable = (Table) fromItem;
                SqlRewriteResult result = doRewrite(entry, parseResult, plainSelect, fromTable);
                if (result != null && CollectionUtils.isNotEmpty(result.getStatements())) {
                    return buildUnionSql(result.getStatements());
                }
            }
        }else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            if(CollectionUtils.isNotEmpty(setOperationList.getSelects())){
                return rewriteSetOperation(entry, setOperationList);
            }
        }
        
        return parseResult.getOriginalSql();
    }

    /**
     * 重写SetOperationList（UNION/UNION ALL/INTERSECT/EXCEPT等）
     * 
     * @param entry 数据源条目
     * @param setOperationList UNION操作列表
     * @return 重写后的SQL
     */
    private String rewriteSetOperation(DataSourceEntry entry, SetOperationList setOperationList) throws JSQLParserException {
        List<SelectBody> originalSelects = setOperationList.getSelects();
        
        List<SelectBody> rewrittenSelects = new ArrayList<>();
        
        for (SelectBody selectBody : originalSelects) {
            SqlParseResult parseResult = sqlParser.parse(selectBody.toString());
            String rewrittenSql = rewrite(entry, parseResult);
            Statement rewrittenStatement = CCJSqlParserUtil.parse(rewrittenSql);
            rewrittenSelects.add(((Select) rewrittenStatement).getSelectBody());
        }
        
        SetOperationList newSetOperationList = new SetOperationList();
        newSetOperationList.setSelects(rewrittenSelects);
        newSetOperationList.setOperations(setOperationList.getOperations());
        
        Select finalSelect = new Select();
        finalSelect.setSelectBody(newSetOperationList);
        
        return finalSelect.toString();
    }

    /**
     * 将多个Statement使用UNION ALL连接
     * 
     * @param statements 重写后的Statement列表
     * @return 使用UNION ALL连接的SQL语句
     */
    private String buildUnionSql(List<Statement> statements) {
        if (statements.size() == 1) {
            return statements.get(0).toString();
        }

        // 创建SetOperationList来组合多个SELECT
        SetOperationList setOperationList = new SetOperationList();
        List<SelectBody> selects = new ArrayList<>();
        List<SetOperation> operations = new ArrayList<>();
        int size = statements.size();
        int last = size - 1;
        for (int i = 0; i < size; i++) {
            Select select = (Select) statements.get(i);
            selects.add(select.getSelectBody());
            if (i < last) {
                // 设置操作类型为UNION ALL
                UnionOp unionOp = new UnionOp().withAll(true);
                operations.add(unionOp);
            }
        }
        setOperationList.setSelects(selects);
        setOperationList.setOperations(operations);

        // 构建最终的SELECT语句
        Select finalSelect = new Select();
        finalSelect.setSelectBody(setOperationList);

        return finalSelect.toString();
    }

    /**
     * 执行 SQL 重写操作的核心方法
     * 
     * 处理逻辑：
     * 1. 首先根据主表名查找对应的分片规则配置
     * 2. 如果找到分片规则，执行分片计算获取分片列表
     * 3. 对每个分片结果，克隆原始 SQL 并替换表名为分片表名
     * 4. 处理 JOIN 表的分片逻辑（支持绑定表关系）
     * 5. 如果没有分片规则或无需分片，返回原始 SQL
     * 
     * @param entry       数据源条目，包含分片规则配置
     * @param parseResult SQL 解析结果
     * @param plainSelect 需要重写的 SELECT 语句主体
     * @param fromTable   主表对象，包含表名
     * @return 重写后的 SQL 结果，包含一个或多个分片后的 SQL 语句
     */
    private SqlRewriteResult doRewrite(DataSourceEntry entry, SqlParseResult parseResult, PlainSelect plainSelect,
            Table fromTable) {
        SqlRewriteResult result = new SqlRewriteResult();
        result.setOriginalSql(parseResult.getOriginalSql());
        String fromTableName = fromTable.getName();

        ShardingRuleConfig fromShardingRuleConfig = findShardingRuleConfig(entry, fromTableName);
        if (fromShardingRuleConfig != null) {
            List<String> shardings = doSharding(fromShardingRuleConfig, fromTable, plainSelect.getWhere());
            if (shardings != null && !shardings.isEmpty()) {
                List<Statement> statements = new ArrayList<>();

                for (String sharding : shardings) {
                    PlainSelect clonedSelect = clonePlainSelect(entry, plainSelect, fromShardingRuleConfig, sharding);

                    Select select = new Select();
                    select.setSelectBody(clonedSelect);
                    statements.add(select);
                }

                result.setStatements(statements);
                return result;
            }
        }

        Select select = new Select();
        select.setSelectBody(plainSelect);
        result.setStatements(List.of(select));
        return result;
    }

    private PlainSelect clonePlainSelect(DataSourceEntry entry, PlainSelect original,
            ShardingRuleConfig fromShardingRuleConfig, String sharding) {
        try {
            String sql = original.toString();
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            PlainSelect cloned = (PlainSelect) select.getSelectBody();

            String newTableName = TableUtils.getPhysicalTableName(fromShardingRuleConfig.getTableName(), sharding);
            String fromShardingColumn = fromShardingRuleConfig.getShardingColumn();

            Table clonedFromTable = (Table) cloned.getFromItem();
            clonedFromTable.setName(newTableName);

            rewriteJoins(entry, cloned, sharding, fromShardingColumn);

            return cloned;
        } catch (JSQLParserException e) {
            throw new RuntimeException("Failed to clone PlainSelect", e);
        }
    }

    private void rewriteJoins(DataSourceEntry entry, PlainSelect plainSelect, String sharding,
            String fromShardingColumn) throws JSQLParserException {
        List<Join> joins = plainSelect.getJoins();
        if (joins == null || joins.isEmpty()) {
            return;
        }

        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                Table joinTable = (Table) rightItem;
                String tableName = joinTable.getName();

                ShardingRuleConfig joinShardingRuleConfig = findShardingRuleConfig(entry, tableName);
                if (joinShardingRuleConfig != null) {
                    String joinShardingColumn = joinShardingRuleConfig.getShardingColumn();

                    boolean isBindingTable = fromShardingColumn != null
                            && fromShardingColumn.equals(joinShardingColumn);

                    if (isBindingTable) {
                        joinTable.setName(tableName + "_" + sharding);
                    }
                }
            } else if (rightItem instanceof SubSelect) {
                SubSelect subSelect = (SubSelect) rightItem;
                SqlParseResult subParseResult = sqlParser.parse(subSelect.getSelectBody().toString());
                String rewrittenSubSql = rewrite(entry, subParseResult);
                Select newSubSelect = (Select) CCJSqlParserUtil.parse(rewrittenSubSql);
                newSubSelect.setSelectBody(((PlainSelect) newSubSelect.getSelectBody()));
            }
        }
    }

    /**
     * 执行分片，获取分片后的表列表
     * 
     * @param shardingRuleConfig 分片规则配置
     * @param table              表
     * @param whereExpression    WHERE 条件
     * @return 分片后的表列表
     */
    private List<String> doSharding(ShardingRuleConfig shardingRuleConfig, Table table, Expression whereExpression) {
        ShardingStrategy strategy = getShardingStrategy(shardingRuleConfig);
        String tableName = shardingRuleConfig.getTableName();
        String shardingColumn = shardingRuleConfig.getShardingColumn();
        try {
            // 从 WHERE 条件中提取分片条件
            List<SqlCondition> conditions = extractConditionsFromWhere(table, shardingColumn, whereExpression);

            // 调用分片策略
            return strategy.doSharding(shardingRuleConfig, conditions);
        } catch (Exception e) {
            logger.error("分片计算失败，使用原表名：{}", tableName, e);
            throw e;
        }
    }

    /**
     * 从 WHERE 条件中提取分片条件
     * 
     * @param table           表
     * @param shardingColumn  分片列名
     * @param whereExpression WHERE 表达式
     * @return 分片条件列表
     */
    private List<SqlCondition> extractConditionsFromWhere(Table table, String shardingColumn,
            Expression whereExpression) {
        List<SqlCondition> conditions = new ArrayList<>();
        if (whereExpression == null) {
            return conditions;
        }
        extractConditionsRecursive(table, shardingColumn, whereExpression, conditions);
        return conditions;
    }

    /**
     * 递归提取条件表达式中的分片键值
     * 
     * @param tableAlias     表别名
     * @param shardingColumn 分片列名
     * @param expression     表达式
     * @param conditions     分片条件列表
     */
    private void extractConditionsRecursive(Table table, String shardingColumn, Expression expression,
            List<SqlCondition> conditions) {
        if (expression == null) {
            return;
        }

        if (expression instanceof AndExpression) {
            AndExpression andExpression = (AndExpression) expression;
            extractConditionsRecursive(table, shardingColumn, andExpression.getLeftExpression(), conditions);
            extractConditionsRecursive(table, shardingColumn, andExpression.getRightExpression(), conditions);
            return;
        }

        if (expression instanceof Parenthesis) {
            Parenthesis parenthesis = (Parenthesis) expression;
            extractConditionsRecursive(table, shardingColumn, parenthesis.getExpression(), conditions);
            return;
        }

        if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            String columnName = getColumnName(equalsTo.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(equalsTo.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.EQUALS, value));
            }
            return;
        }

        if (expression instanceof InExpression) {
            InExpression inExpression = (InExpression) expression;
            if (inExpression.getRightItemsList() instanceof SubSelect) {
                return;
            }
            String columnName = getColumnName(inExpression.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getListValue(inExpression.getRightItemsList());
                conditions.add(new SqlCondition(columnName, ConditionType.IN, value));
            }
            return;
        }

        if (expression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) expression;
            String columnName = getColumnName(notEqualsTo.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(notEqualsTo.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.NOT_EQUALS, value));
            }
            return;
        }

        if (expression instanceof GreaterThan) {
            GreaterThan greaterThan = (GreaterThan) expression;
            String columnName = getColumnName(greaterThan.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(greaterThan.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.GREATER_THAN, value));
            }
            return;
        }

        if (expression instanceof GreaterThanEquals) {
            GreaterThanEquals greaterThanEquals = (GreaterThanEquals) expression;
            String columnName = getColumnName(greaterThanEquals.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(greaterThanEquals.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.GREATER_THAN_OR_EQUAL, value));
            }
            return;
        }

        if (expression instanceof MinorThan) {
            MinorThan lessThan = (MinorThan) expression;
            String columnName = getColumnName(lessThan.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(lessThan.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.LESS_THAN, value));
            }
            return;
        }

        if (expression instanceof MinorThanEquals) {
            MinorThanEquals lessThanEquals = (MinorThanEquals) expression;
            String columnName = getColumnName(lessThanEquals.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(lessThanEquals.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.LESS_THAN_OR_EQUAL, value));
            }
            return;
        }

        if (expression instanceof LikeExpression) {
            LikeExpression likeExpression = (LikeExpression) expression;
            String columnName = getColumnName(likeExpression.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                Object value = getValue(likeExpression.getRightExpression());
                conditions.add(new SqlCondition(columnName, ConditionType.LIKE, value));
            }
            return;
        }

        if (expression instanceof IsNullExpression) {
            IsNullExpression isNullExpression = (IsNullExpression) expression;
            String columnName = getColumnName(isNullExpression.getLeftExpression(), table, shardingColumn);
            if (columnName != null) {
                ConditionType conditionType = isNullExpression.isNot() ? ConditionType.IS_NOT_NULL
                        : ConditionType.IS_NULL;
                conditions.add(new SqlCondition(columnName, conditionType, null));
            }
            return;
        }
    }

    /**
     * 从表达式中获取列名，并判断是否匹配分片键
     * 
     * @param expression     表达式
     * @param tableAlias     表别名
     * @param shardingColumn 分片列名
     * @return 如果匹配分片键返回列名，否则返回 null
     */
    private String getColumnName(Expression expression, Table table, String shardingColumn) {
        if (expression instanceof Column) {
            Column column = (Column) expression;
            String columnName = column.getName(false);

            boolean matchesColumn = shardingColumn != null && shardingColumn.equalsIgnoreCase(columnName);

            boolean matchesTable;
            if (table.getAlias() == null || table.getAlias().getName().isEmpty()) {
                matchesTable = column.getTable() == null || column.getTable().getAlias() == null
                        || column.getTable().getAlias().getName().isEmpty();
            } else {
                matchesTable = column.getTable() != null && table.getAlias().getName()
                        .equalsIgnoreCase(column.getTable().getAlias().getName());
            }

            if (matchesColumn && matchesTable) {
                return columnName;
            }
        }
        return null;
    }

    /**
     * 从表达式中获取值
     */
    private Object getValue(Expression expression) {
        if (expression == null) {
            return null;
        }
        if (expression instanceof LongValue) {
            return ((LongValue) expression).getValue();
        }
        if (expression instanceof DoubleValue) {
            return ((DoubleValue) expression).getValue();
        }
        if (expression instanceof StringValue) {
            return ((StringValue) expression).getValue();
        }
        if (expression instanceof ValueListExpression) {
            return expression.toString();
        }
        if (expression instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) expression;
            List<Object> values = new ArrayList<>();
            for (Expression exp : expressionList.getExpressions()) {
                values.add(getValue(exp));
            }
            return values;
        }
        return expression.toString();
    }

    /**
     * 从ItemsList中获取列表值列表
     */
    private Object getListValue(ItemsList itemsList) {
        List<Object> values = new ArrayList<>();
        if (itemsList == null) {
            return values;
        }
        if (itemsList instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) itemsList;
            for (Expression exp : expressionList.getExpressions()) {
                values.add(getValue(exp));
            }
        }
        return values;
    }

    /**
     * 根据数据源条目和表名查找分片规则配置
     * 
     * @param entry     数据源条目
     * @param tableName 表名
     * @return 分片规则配置
     */
    private ShardingRuleConfig findShardingRuleConfig(DataSourceEntry entry, String tableName) {
        if (CollectionUtils.isNotEmpty(entry.getShardingRuleConfigs())) {
            for (ShardingRuleConfig ruleConfig : entry.getShardingRuleConfigs()) {
                if (ruleConfig.getTableName().equals(tableName)) {
                    return ruleConfig;
                }
            }
        }

        return null;
    }

    /**
     * 根据数据源条目和表名获取分片策略
     * 
     * @param entry     数据源条目
     * @param tableName 表名
     * @return 分片策略
     */
    private ShardingStrategy getShardingStrategy(ShardingRuleConfig shardingRuleConfig) {
        if (shardingRuleConfig == null) {
            return null;
        }
        String strategyType = shardingRuleConfig.getStrategyType();
        ShardingStrategy shardingStrategy = ShardingStrategyFactory.getShardingStrategy(strategyType);
        if (shardingStrategy == null) {
            throw new RuntimeException("无法从 Spring 容器获取分片策略：" + strategyType);
        }

        return shardingStrategy;
    }

}