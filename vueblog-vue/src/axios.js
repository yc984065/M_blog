import axios from "axios";
import Element from "element-ui";
import router from "./router";
import store from "./store";

axios.defaults.baseURL = "http://localhost:8081";

// 前置拦截
axios.interceptors.request.use(
  (config) => {
    if (localStorage.getItem("token")) {
      config.headers.Authorization = localStorage.getItem("token");
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
axios.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response.status === 401) {
        // token过期，跳转到登录页
        localStorage.removeItem('token');
        router.push('/login');
    }
    return Promise.reject(error);
  },
  (error) => {
    console.log(error);
    if (error.response.data) {
      error.message = error.response.data.msg;
    }

    if (error.response.status === 401) {
      store.commit("REMOVE_INFO"); // vuex store 删除token
      localStorage.removeItem("token"); //localStorage 删除token
      router.push("/login"); // 跳转登录页面
    }

    Element.Message.error(error.message, { duration: 3000 });
    return Promise.reject(error);
  }
);

export default axios;