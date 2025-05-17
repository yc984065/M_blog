<template>
  <div>
    <Header />
    <div class="admin-keywords-container">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="margin-bottom: 20px;">
        <el-breadcrumb-item :to="{ name: 'AdminDashboard' }">管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>关键字管理</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>关键字管理</h2>
      <p>管理系统中用于内容过滤的违禁词或敏感词汇。</p>
      
      <el-form :inline="true" @submit.native.prevent="handleAddKeyword" class="keyword-form">
        <el-form-item label="新关键字">
          <el-input v-model="newKeywordText" placeholder="输入违禁词" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="addLoading" icon="el-icon-plus">添加</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="keywordsList" v-loading="listLoading" style="width: 100%" class="keywords-table" border stripe empty-text="暂无关键字数据">
        <el-table-column prop="id" label="ID" width="100" align="center" sortable></el-table-column>
        <el-table-column prop="keyword" label="关键字" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="created" label="创建时间" width="180" align="center" sortable>
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.created) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="danger"
              @click="handleDeleteKeyword(scope.row.id, scope.row.keyword)"
              :loading="scope.row.deleteLoading"
              icon="el-icon-delete"
              title="删除关键字"
            ></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import Header from '@/components/Header.vue';

export default {
  components: { Header },
  name: "AdminKeywords",
  data() {
    return {
      newKeywordText: "",
      keywordsList: [],
      listLoading: false,
      addLoading: false,
    };
  },
  created() {
    console.log('AdminKeywords component CREATED');
    this.fetchKeywords();
  },
  methods: {
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return 'N/A';
      try {
        const date = new Date(dateTimeStr);
        if (isNaN(date.getTime())) return '无效日期';
        return date.toLocaleString('zh-CN', { 
          year: 'numeric', month: '2-digit', day: '2-digit', 
          hour: '2-digit', minute: '2-digit', second: '2-digit',
          hour12: false 
        });
      } catch (e) {
        return dateTimeStr;
      }
    },
    fetchKeywords() {
      this.listLoading = true;
      this.$axios.get("/admin/keywords/list") 
        .then(res => {
          if (res.data.code === 200) {
            this.keywordsList = res.data.data.map(kw => ({ ...kw, deleteLoading: false }));
          } else {
            this.$message.error(res.data.msg || "获取关键字列表失败");
          }
        })
        .catch(error => {
          console.error("获取关键字列表请求失败:", error.response || error.message);
          this.$message.error(error.response?.data?.msg || "获取关键字列表请求失败，请检查网络或联系管理员");
        })
        .finally(() => {
          this.listLoading = false;
        });
    },
    handleAddKeyword() {
      if (!this.newKeywordText.trim()) {
        this.$message.warning("关键字不能为空");
        return;
      }
      this.addLoading = true;
      this.$axios.post("/admin/keywords/add", { keyword: this.newKeywordText.trim() })
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success("关键字添加成功");
            this.newKeywordText = "";
            this.fetchKeywords(); // 重新加载列表
          } else {
            this.$message.error(res.data.msg || "添加关键字失败");
          }
        })
        .catch(error => {
          console.error("添加关键字请求失败:", error.response || error.message);
          this.$message.error(error.response?.data?.msg || "添加关键字请求失败");
        })
        .finally(() => {
          this.addLoading = false;
        });
    },
    handleDeleteKeyword(id, keywordText) {
      this.$confirm(`确定要永久删除关键字“${keywordText || '此关键字'}”吗?`, '警告：删除关键字', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const keywordIndex = this.keywordsList.findIndex(kw => kw.id === id);
        if (keywordIndex !== -1) {
          this.$set(this.keywordsList[keywordIndex], 'deleteLoading', true);
        }
        
        this.$axios.delete(`/admin/keywords/delete/${id}`)
          .then(res => {
            if (res.data.code === 200) {
              this.$message.success("关键字删除成功");
              this.fetchKeywords(); // 重新加载列表
            } else {
              this.$message.error(res.data.msg || "删除关键字失败");
              if (keywordIndex !== -1) this.$set(this.keywordsList[keywordIndex], 'deleteLoading', false);
            }
          })
          .catch(error => {
            console.error("删除关键字请求失败:", error.response || error.message);
            this.$message.error(error.response?.data?.msg || "删除关键字请求失败");
            if (keywordIndex !== -1) this.$set(this.keywordsList[keywordIndex], 'deleteLoading', false);
          });
      }).catch(() => {
        this.$message.info('已取消删除操作');
      });
    }
  }
};
</script>

<style scoped>
.admin-keywords-container {
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.admin-keywords-container > h2 { 
  margin-bottom: 10px;
  font-size: 22px;
  color: #303133;
}
.admin-keywords-container > p {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}
.keyword-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}
.keywords-table {
  margin-top: 20px;
}
.el-table-column .el-button--mini {
  padding: 5px 10px;
}
</style>
