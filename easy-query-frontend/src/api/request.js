import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error(res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  error => {
    console.error('Network error:', error)
    return Promise.reject(error)
  }
)

export default request
