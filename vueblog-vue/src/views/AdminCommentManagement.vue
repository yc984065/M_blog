<template>
  <div class="admin-comment-management-page">
    <Header />
    <div class="page-container">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="margin-bottom: 20px;">
        <el-breadcrumb-item :to="{ name: 'AdminDashboard' }">管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>评论管理</el-breadcrumb-item>
      </el-breadcrumb>

      <h2>所有评论列表</h2>
      <p>在这里您可以查看、搜索并管理系统中所有用户发布的评论。</p>

      <el-form :inline="true" :model="filters" @submit.native.prevent="handleSearch" class="search-form">
        <el-form-item label="评论内容搜索">
          <el-input v-model="filters.queryContent" placeholder="输入评论内容关键字" clearable @clear="handleSearchOnClear"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
          <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="comments"
        style="width: 100%"
        border
        stripe
        empty-text="暂无评论数据"
      >
        <el-table-column prop="id" label="评论ID" width="90" align="center" sortable></el-table-column>
        <el-table-column label="评论内容" min-width="250" show-overflow-tooltip>
            <template slot-scope="scope">
                <div class="comment-content-cell">{{ scope.row.content }}</div>
            </template>
        </el-table-column>
        <el-table-column label="所属博客" width="200" show-overflow-tooltip>
            <template slot-scope="scope">
                <el-link type="primary" @click="viewBlog(scope.row.blogId)" :title="scope.row.blogTitle">
                    {{ scope.row.blogTitle || '博客ID: ' + scope.row.blogId }}
                </el-link>
            </template>
        </el-table-column>
        <el-table-column label="评论者" width="150">
            <template slot-scope="scope">
                <div>{{ scope.row.username || '匿名' }} (ID: {{ scope.row.userId }})</div>
                <el-tag size="mini" type="warning" effect="dark" v-if="scope.row.userRole === 'admin'" class="admin-tag-table">
                  管理员
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="170" align="center" sortable>
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleDeleteComment(scope.row.id, scope.row.content)"
              :loading="scope.row.deleteLoading"
              title="删除评论"
            ></el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 20px; text-align: right;"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalComments"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        v-if="totalComments > 0"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import Header from '@/components/Header.vue'; 
import { mapGetters } from 'vuex'; // 如果需要获取当前管理员信息

export default {
  name: 'AdminCommentManagement',
  components: { Header },
  data() {
    return {
      comments: [],
      loading: false,
      currentPage: 1,
      pageSize: 10, 
      totalComments: 0,
      filters: { 
        queryContent: '', // 用于存储评论内容的搜索条件
      }
    };
  },
  computed: {
    ...mapGetters(['isAdmin']), // 确保管理员才能访问此页面 (通过路由守卫)
  },
  created() {
    this.fetchComments();
  },
  methods: {
    fetchComments() {
      this.loading = true;
      const params = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        queryContent: this.filters.queryContent ? this.filters.queryContent.trim() : ''
      };
      
      if (params.queryContent === '') {
        delete params.queryContent;
      }
      
      console.log('Fetching comments with params:', JSON.parse(JSON.stringify(params))); 

      this.$axios.get('/api/admin/comments/all', { params }) // 调用后端管理员获取所有评论的API
      .then(res => {
        if (res.data.code === 200) {
          const pageData = res.data.data; // 假设后端返回 IPage<CommentDto>
          this.comments = pageData.records.map(comment => ({ ...comment, deleteLoading: false }));
          this.totalComments = pageData.total;
        } else {
          this.$message.error(res.data.msg || '获取评论列表失败');
        }
      })
      .catch(error => {
        console.error("获取评论列表失败:", error.response || error);
        this.$message.error(error.response?.data?.msg || '请求评论列表失败');
      })
      .finally(() => {
        this.loading = false;
      });
    },
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
    viewBlog(blogId) {
      if (blogId) {
        this.$router.push({ name: 'BlogDetail', params: { blogId: blogId } });
      } else {
        this.$message.info('该评论没有关联到具体的博客');
      }
    },
    handleDeleteComment(commentId, commentContentSnippet) {
      const snippet = commentContentSnippet && commentContentSnippet.length > 20 
                      ? commentContentSnippet.substring(0, 20) + '...' 
                      : commentContentSnippet;
      this.$confirm(`确定要永久删除这条评论吗? <br/>内容片段: "<strong>${snippet || '该评论'}</strong>" <br/>此操作不可恢复。`, '警告：删除评论', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true, // 允许在消息中使用HTML
        center: true
      }).then(() => {
        const commentIndex = this.comments.findIndex(c => c.id === commentId);
        if (commentIndex !== -1) {
          this.$set(this.comments[commentIndex], 'deleteLoading', true);
        }
        
        // 后端 CommentController 的 deleteComment 接口已包含管理员权限判断
        this.$axios.delete(`/api/comments/${commentId}`)
          .then(res => {
            if (res.data.code === 200) {
              this.$message.success('评论删除成功');
              if (this.comments.length === 1 && this.currentPage > 1) {
                this.currentPage--;
              }
              this.fetchComments(); 
            } else {
              this.$message.error(res.data.msg || '删除评论失败');
            }
          })
          .catch(error => {
            console.error("删除评论请求失败:", error);
            this.$message.error(error.response?.data?.msg || '请求删除评论失败');
          })
          .finally(() => {
            if (commentIndex !== -1 && this.comments[commentIndex]) { 
              this.$set(this.comments[commentIndex], 'deleteLoading', false);
            }
          });
      }).catch(() => {
        // this.$message.info('已取消删除操作');
      });
    },
    handlePageChange(newPage) {
      this.currentPage = newPage;
      this.fetchComments();
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.currentPage = 1; 
      this.fetchComments();
    },
    handleSearch() {
      console.log('[Search Button Clicked] Current filters.queryContent:', this.filters.queryContent);
      this.currentPage = 1; 
      this.fetchComments();
    },
    handleSearchOnClear() {
      // 当 el-input 的 clearable 图标被点击时，v-model (filters.queryContent) 会被 el-input 内部设置为空字符串。
      // 然后 @clear 事件触发，调用此方法。
      // 此时 filters.queryContent 已经是 ''，直接调用 handleSearch 即可。
      this.handleSearch();
    },
    resetSearch() {
      console.log('[Reset Button Clicked] Resetting filters.queryContent.');
      this.filters.queryContent = ''; 
      this.currentPage = 1;
      this.fetchComments();
    }
  },
};
</script>

<style scoped>
.admin-comment-management-page {
  display: flex;
  flex-direction: column;
}
.page-container {
  padding: 20px;
  max-width: 1200px; 
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
h2 {
  margin-bottom: 10px;
  font-size: 22px;
  color: #303133;
}
.page-container > p { 
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}
.search-form { 
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}
.search-form .el-form-item {
  margin-bottom: 0; 
}
.el-table .el-link {
  font-weight: normal; 
}
.el-table-column .el-button--mini { 
  padding: 5px 10px;
}
.comment-content-cell { /* 用于控制评论内容在表格中的显示 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.admin-tag-table { /* 表格内管理员标签样式 */
    margin-top: 4px;
    display: inline-block;
}
</style>
