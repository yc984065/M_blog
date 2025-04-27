<template>
  <div class="m-content">
    <h3>欢迎来到Mrblue的博客</h3>
    <div class="block">
      <el-avatar :size="50" :src="avatarUrl"></el-avatar>
      <div>{{ user.username }}</div>
    </div>

    <div class="maction">
      <span><el-link href="/blogs">主页</el-link></span>
      <el-divider direction="vertical"></el-divider>
      <span><el-link type="success" href="/blog/add">发表博客</el-link></span>

      <el-divider direction="vertical"></el-divider>
      <span v-show="!hasLogin"><el-link type="primary" href="/login">登录</el-link></span>

      <span v-show="hasLogin"><el-link type="danger" @click="logout">退出</el-link></span>
    </div>
  </div>
</template>

<script>
export default {
  name: "Header",
  data() {
    return {
      user: {
        username: '请先登录',
        avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      },
      hasLogin: false
    }
  },
  computed: {
    avatarUrl() {
      // 如果头像路径以 /uploads/ 开头，则拼接后端端口号
      if (this.user.avatar && this.user.avatar.startsWith('/uploads/')) {
        return `http://localhost:8081${this.user.avatar}`;
      }
      // 否则，假设头像路径是外部 URL 或相对路径，直接返回
      return this.user.avatar;
    },
  },
  methods: {
    logout() {
      const _this = this;
      _this.$axios.get("/logout", {
        headers: {
          "Authorization": localStorage.getItem("token")
        }
      }).then(res => {
        _this.$store.commit("REMOVE_INFO");
        localStorage.removeItem("token"); // 移除本地存储的 token
        _this.$router.push("/login");
      }).catch(error => {
        console.error("退出失败:", error);
        localStorage.removeItem("token"); // 移除本地存储的 token
        _this.$router.push("/login");
      });
    }
  },
  created() {
    if (this.$store.getters.getUser.username) {
      this.user.username = this.$store.getters.getUser.username
      this.user.avatar = this.$store.getters.getUser.avatar

      this.hasLogin = true
    }
  }
}
</script>

<style scoped>
.m-content {
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
}
.maction {
  margin: 10px 0;
}
</style>