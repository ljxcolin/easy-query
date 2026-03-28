package com.easyquery.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.FromItem;

public class SelectSqlParseTest {

    @Test
    public void testParseSelectSql1() throws Exception {
        // String sql = "SELECT * FROM t_user WHERE id = 1";
        // String sql = "SELECT id, name FROM t_user WHERE id = 1 AND name = 'colin'";
        // String sql = "SELECT u.id, u.name FROM t_user u WHERE u.id = 1 AND u.name = 'colin' AND u.status = 1";
        // String sql = "SELECT u.id, u.name FROM t_user u WHERE u.id = 1 OR u.name = 'colin'";
        // String sql = "SELECT u.id, u.name FROM t_user u WHERE u.id BETWEEN 1 AND 10";
        String sql = "SELECT u.id, u.name FROM t_user u WHERE u.id IN (1, 2, 3)";
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            
            // 提取SELECT列 [*] [id, name]
            System.out.println(plainSelect.getSelectItems());

            // 提取FROM子句 t_user
            System.out.println(plainSelect.getFromItem().toString());

            // 提取WHERE条件 id = 1 | id = 1 AND name = 'colin'
            Expression where = plainSelect.getWhere();
            if (where != null) {
                System.out.println(where.toString());
            }
        }
    }

    @Test
    public void testParseSelectSql2() throws Exception {
        // String sql = """
        // SELECT o.order_id, o.user_id, u.user_name FROM t_order o
        // LEFT JOIN t_user u ON u.id = o.user_id
        // WHERE o.order_id = 1 AND o.user_id = 1
        // """;
        // String sql = """
        // SELECT o.order_id, o.user_id, u.user_name FROM (SELECT * FROM t_order WHERE order_id = 1) o
        // LEFT JOIN t_user u ON u.id = o.user_id
        // WHERE o.order_id = 1 AND o.user_id = 1
        // """;
        String sql = """
        SELECT o.order_id, o.user_id, u.user_name FROM t_order o
        LEFT JOIN (SELECT * FROM t_user WHERE id = 1) u ON u.id = o.user_id
        WHERE o.order_id = 1 AND o.user_id = 1
        """;
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            
            // 提取SELECT列 [*] [id, name]
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            System.out.println(selectItems);

            // 提取FROM子句 t_user
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println(fromItem.toString());
            
            // 提取JOIN条件
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    System.out.println(join.toString());
                    System.out.println(join.getOnExpressions());
                }
            }

            // 提取WHERE条件 id = 1 | id = 1 AND name = 'colin'
            Expression where = plainSelect.getWhere();
            if (where != null) {
                System.out.println(where.toString());
            }
            
            // 提取GROUP BY子句
            if (plainSelect.getGroupBy() != null) {
                System.out.println(plainSelect.getGroupBy().toString());
            }
            
            // 提取ORDER BY子句
            if (plainSelect.getOrderByElements() != null) {
                System.out.println(plainSelect.getOrderByElements().toString());
            }
            
            // 提取LIMIT子句
            if (plainSelect.getLimit() != null) {
                System.out.println(plainSelect.getLimit().toString());
            }
        }
    }

    @Test
    public void testParseSelectSql3() throws Exception {
        String sql = """
        (SELECT o.order_id, o.user_id, u.user_name FROM t_order o
        LEFT JOIN t_user u ON u.id = o.user_id
        WHERE o.order_id = 1 
        ) UNION ALL
        (SELECT o.order_id, o.user_id, u.user_name FROM t_order o
        LEFT JOIN t_user u ON u.id = o.user_id
        WHERE o.order_id = 2 
        ) UNION ALL
        (SELECT o.order_id, o.user_id, u.user_name FROM t_order o
        LEFT JOIN t_user u ON u.id = o.user_id
        WHERE o.order_id = 3 
        )
        """;
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            
            // 提取SELECT列 [*] [id, name]
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            System.out.println(selectItems);

            // 提取FROM子句 t_user
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println(fromItem.toString());
            
            // 提取JOIN条件
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    System.out.println(join.toString());
                    System.out.println(join.getOnExpressions());
                }
            }

            // 提取WHERE条件 id = 1 | id = 1 AND name = 'colin'
            Expression where = plainSelect.getWhere();
            if (where != null) {
                System.out.println(where.toString());
            }
            
            // 提取GROUP BY子句
            if (plainSelect.getGroupBy() != null) {
                System.out.println(plainSelect.getGroupBy().toString());
            }
            
            // 提取ORDER BY子句
            if (plainSelect.getOrderByElements() != null) {
                System.out.println(plainSelect.getOrderByElements().toString());
            }
            
            // 提取LIMIT子句
            if (plainSelect.getLimit() != null) {
                System.out.println(plainSelect.getLimit().toString());
            }
        }
    }

}
