import router from "./router";
import store from './store'; // 确保导入 store
import Element from "element-ui"; // 用于消息提示

router.beforeEach((to, from, next) => {
  console.log(`Navigating from: ${from.path} to: ${to.path}`); // 记录导航目标

  const token = store.state.token;
  const userInfo = store.getters.getUser; // 从 Vuex 获取用户信息

  console.log('Permission Guard Check:', { 
    tokenExists: !!token, 
    userInfo: userInfo, // 打印完整的 userInfo
    targetPath: to.path,
    requiresAuth: to.matched.some(record => record.meta.requireAuth),
    requiresAdmin: to.matched.some(record => record.meta.requireAdmin)
  });

  if (to.matched.some(record => record.meta.requireAuth)) { // 路由是否需要认证
    console.log('Route requires authentication.');
    if (token && userInfo && userInfo.id) { // 用户已登录 (有 token 且 userInfo 有效)
      console.log('User is authenticated.');
      
      // 检查路由是否需要管理员权限
      if (to.matched.some(record => record.meta.requireAdmin)) {
        console.log('Route requires admin privileges.');
        if (userInfo.role === 'admin') {
          console.log('User is admin. Allowing navigation.');
          next(); // 用户是管理员，允许访问
        } else {
          // 用户不是管理员，但试图访问管理员页面
          console.log('User is not admin. Redirecting to UserPage.');
          Element.Message.error("您没有权限访问此页面！");
          next({ name: 'UserPage' }); // 重定向到普通用户主页
        }
      } else {
        // 路由需要认证，但不需要管理员权限 (普通用户页面)
        console.log('Route does not require admin. Checking for login page access...');
        if (to.name === 'Login' || to.name === 'Register') { // 如果已登录用户尝试访问登录/注册页
           console.log('User is logged in and trying to access Login/Register. Redirecting based on role.');
           // 根据角色重定向到各自的首页
          next(userInfo.role === 'admin' ? { name: 'AdminDashboard' } : { name: 'UserPage' });
        } else {
          console.log('Allowing navigation to non-admin, authenticated route.');
          next(); // 正常访问
        }
      }
    } else {
      // 用户未登录，但路由需要认证
      console.log('User is not authenticated. Redirecting to Login.');
      store.commit("REMOVE_INFO"); // 清理可能存在的无效登录信息
      Element.Message.warning("请先登录后再访问！");
      next({
        name: 'Login',
        query: { redirect: to.fullPath } // 保存目标路径，登录后可以跳回
      });
    }
  } else {
    // 路由不需要认证 (例如登录页、注册页本身)
    console.log('Route does not require authentication.');
     if ((to.name === 'Login' || to.name === 'Register') && token && userInfo && userInfo.id) { // 如果已登录用户尝试访问登录/注册页
        console.log('User is logged in and trying to access public Login/Register. Redirecting based on role.');
        // 根据角色重定向到各自的首页
        next(userInfo.role === 'admin' ? { name: 'AdminDashboard' } : { name: 'UserPage' });
    } else {
        console.log('Allowing navigation to public route.');
        next(); // 正常访问
    }
  }
});

// 添加一个 afterEach 钩子，可以帮助确认导航是否真的完成了
router.afterEach((to, from) => {
  console.log(`Navigation successful to: ${to.path}`);
});

// 添加一个 onError 钩子，捕获路由过程中的异步错误
router.onError(error => {
  console.error('Router error:', error);
});