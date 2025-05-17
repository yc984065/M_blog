<template>
    <div class="admin-blog-management-page">
      <Header />
      <div class="page-container">
        <el-breadcrumb separator-class="el-icon-arrow-right" style="margin-bottom: 20px;">
          <el-breadcrumb-item :to="{ name: 'AdminDashboard' }">管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>博客管理</el-breadcrumb-item>
        </el-breadcrumb>
  
        <h2>所有博客列表</h2>
        <p>在这里您可以查看、搜索并管理系统中所有用户发布的博客。</p>
  
        <el-form :inline="true" :model="filters" @submit.native.prevent="handleSearch" class="search-form">
          <el-form-item label="标题搜索">
            <el-input v-model="filters.query" placeholder="输入博客标题关键字" clearable @clear="handleSearch"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
            <el-button @click="resetSearch" icon="el-icon-refresh">重置</el-button>
          </el-form-item>
        </el-form>
  
        <el-table
          v-loading="loading"
          :data="blogs"
          style="width: 100%"
          border
          stripe
          empty-text="暂无博客数据"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" sortable></el-table-column>
          <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="viewBlog(scope.row.id)" title="查看博客详情">{{ scope.row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="作者信息" width="180">
              <template slot-scope="scope">
                  <div>ID: {{ scope.row.userId }}</div>
                  </template>
          </el-table-column>
          <el-table-column prop="created" label="发布时间" width="170" align="center" sortable>
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.created) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
              <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
                      {{ scope.row.status === 0 ? '已发布' : '草稿' }}
                  </el-tag>
              </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="danger"
                icon="el-icon-delete"
                @click="handleDelete(scope.row.id, scope.row.title)"
                :loading="scope.row.deleteLoading"
                title="删除博客"
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
          :total="totalBlogs"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          v-if="totalBlogs > 0"
        >
        </el-pagination>
      </div>
    </div>
  </template>
  
  <script>
  import Header from '@/components/Header.vue'; 
  
  export default {
    name: 'AdminBlogManagement',
    components: { Header },
    data() {
      return {
        blogs: [],
        loading: false,
        currentPage: 1,
        pageSize: 10, 
        totalBlogs: 0,
        filters: { 
          query: '',
        }
      };
    },
    created() {
      this.fetchBlogs();
    },
    methods: {
      fetchBlogs() {
        this.loading = true;
        const params = {
          currentPage: this.currentPage,
          pageSize: this.pageSize,
          query: this.filters.query.trim() 
        };
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key];
          }
        });
  
        this.$axios.get('/admin/blogs/all', { params }) 
        .then(res => {
          if (res.data.code === 200) {
            const pageData = res.data.data;
            this.blogs = pageData.records.map(blog => ({ ...blog, deleteLoading: false }));
            this.totalBlogs = pageData.total;
          } else {
            this.$message.error(res.data.msg || '获取博客列表失败');
          }
        })
        .catch(error => {
          console.error("获取博客列表失败:", error);
          this.$message.error(error.response?.data?.msg || '请求博客列表失败');
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
        this.$router.push({ name: 'BlogDetail', params: { blogId: blogId } });
      },
      handleDelete(blogId, blogTitle) {
        this.$confirm(`确定要永久删除博客《${blogTitle || '此博客'}》吗? 此操作不可恢复。`, '警告：删除博客', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          const blogIndex = this.blogs.findIndex(b => b.id === blogId);
          if (blogIndex !== -1) {
            this.$set(this.blogs[blogIndex], 'deleteLoading', true);
          }
          
          this.$axios.delete(`/blog/${blogId}`)
            .then(res => {
              if (res.data.code === 200) {
                this.$message.success('博客删除成功');
                if (this.blogs.length === 1 && this.currentPage > 1) {
                  this.currentPage--;
                }
                this.fetchBlogs(); 
              } else {
                this.$message.error(res.data.msg || '删除博客失败');
              }
            })
            .catch(error => {
              console.error("删除博客请求失败:", error);
              this.$message.error(error.response?.data?.msg || '请求删除博客失败');
            })
            .finally(() => {
              if (blogIndex !== -1 && this.blogs[blogIndex]) { // 再次检查 blog 是否存在
                this.$set(this.blogs[blogIndex], 'deleteLoading', false);
              }
            });
        }).catch(() => {
          // this.$message.info('已取消删除操作');
        });
      },
      handlePageChange(newPage) {
        this.currentPage = newPage;
        this.fetchBlogs();
      },
      handleSizeChange(newSize) {
        this.pageSize = newSize;
        this.currentPage = 1; 
        this.fetchBlogs();
      },
      handleSearch() {
        this.currentPage = 1; // 搜索时通常重置到第一页
        this.fetchBlogs();
      },
      resetSearch() {
        this.filters.query = '';
        this.currentPage = 1;
        this.fetchBlogs();
      }
    },
  };
  </script>
  
  <style scoped>
  .admin-blog-management-page {
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
  </style>
  