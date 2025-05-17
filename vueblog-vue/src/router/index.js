import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import UserPage from '../views/User.vue'
import BlogEdit from '../views/BlogEdit.vue'
import BlogDetail from '../views/BlogDetail.vue'
import Register from '../views/Register.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminKeywords from '../views/AdminKeywords.vue' 
import AdminBlogManagement from '../views/AdminBlogManagement.vue'
import AdminCommentManagement from '../views/AdminCommentManagement.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Index',
    redirect: {name: "UserPage"}
  },
  {
    path: '/user',
    name: 'UserPage',
    component: UserPage,
    meta: {
      requireAuth: true // 用户主页通常需要登录
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/blog/add',
    name: 'BlogAdd',
    component: BlogEdit,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/blog/:blogId',
    name: 'BlogDetail',
    component: BlogDetail
  },
  {
    path: '/blog/:blogId/edit',
    name: 'BlogEdit',
    component: BlogEdit,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },

  {
    path: '/dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: {
          requireAuth: true, 
          requireAdmin: true
        }
  },
  {
    path: '/admin/keywords',
    name: 'AdminKeywords',
    component: AdminKeywords,
    meta: {
      requireAuth: true,
      requireAdmin: true
    }
  },
  {
    path: '/admin/blogs', 
    name: 'AdminBlogManagement',
    component: AdminBlogManagement,
    meta: {
      requireAuth: true,
      requireAdmin: true,
      title: '博客管理' 
    }
  },
  {
    path: '/admin/comments',
    name: 'AdminCommentManagement',
    component: AdminCommentManagement,
    meta: {
      requireAuth: true,
      requireAdmin: true,
      title: '评论管理'
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
