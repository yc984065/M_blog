<template>
  <div>
    <el-container>
      <el-header>
        <img class="mlogo" src="https://www.markerhub.com/dist/images/logo/markerhub-logo.png" alt="">
      </el-header>
      <el-main>
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="ruleForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input type="password" v-model="ruleForm.password"></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">登录</el-button>
            <el-button @click="resetForm('ruleForm')">重置</el-button>
          </el-form-item>
          <el-form-item>
            <el-link type="primary" href="/register">没有账号？立即注册</el-link>
          </el-form-item>
          <el-form-item>
            <el-button @click="goHome">返回主页</el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      ruleForm: {
        username: "Mrblue", 
        password: "984065220",
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 3, max: 15, message: "长度在 3 到 15 个字符", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" } 
        ],
      },
      loading: false, 
    };
  },
  methods: {
    submitForm(formName) {
  this.$refs[formName].validate((valid) => {
    if (valid) {
      this.$axios.post("/login", this.ruleForm).then((res) => {
        if (res.data.code === 200 && res.headers["authorization"]) {
          const jwt = res.headers["authorization"];
          const userInfo = res.data.data;
          console.log("登录成功，用户信息:", userInfo);
          
          this.$store.commit("SET_TOKEN", jwt);
          this.$store.commit("SET_USERINFO", userInfo);

          // 根据角色跳转
          if (userInfo && userInfo.role === 'admin') {
            this.$message.success("管理员登录成功！正在跳转到管理后台...");
            this.$router.push({ name: 'AdminDashboard' });
          } else {
            this.$message.success("登录成功！");
            this.$router.push({ name: 'UserPage' }); // 跳转到用户主页
          }
        } else {
          this.$message.error(res.data.msg || '登录失败，请检查您的凭据');
        }
      }).catch(err => {
        console.error("登录请求失败:", err);
        const errorMsg = err.response?.data?.msg || err.message || '登录请求异常';
        this.$message.error(errorMsg);
      });
    } else {
      console.log("登录表单校验失败");
      return false;
    }
  });
},
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    goHome() {
      const userInfo = this.$store.getters.getUser;

        this.$router.push({ name: 'UserPage' }); // 默认用户主页
      
    },
  },
};
</script>

<style scoped>
.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 200px;
}

.el-main {
  color: #333;
  text-align: center;
  line-height: 160px;
}

body > .el-container {
  margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
  line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
  line-height: 320px;
}

.mlogo {
  height: 60%;
  margin-top: 10px;
}

.demo-ruleForm {
  max-width: 500px;
  margin: 0 auto;
}
</style>