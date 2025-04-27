<template>
  <div class="mcontainer">
    <Header />
    <div class="main-content">
      <!-- 搜索栏 + 分类 + 标签 -->
      <div class="filter-container">
        <!-- 搜索输入框 -->
        <el-input
          v-model="searchQuery"
          placeholder="🔍 搜索博客标题..."
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        ></el-input>

        <!-- 搜索按钮 -->
        <el-button
          type="primary"
          icon="el-icon-search"
          @click="handleSearch"
          class="search-button"
        >
          搜索
        </el-button>

        <!-- 分类下拉 -->
        <el-select
          v-model="selectedCategory"
          placeholder="📂 分类"
          @change="handleSearch"
          clearable
          class="select-box"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <!-- 标签下拉 -->
        <el-select
          v-model="selectedTag"
          placeholder="🏷️ 标签"
          @change="handleSearch"
          clearable
          class="select-box"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>

        <!-- 重置按钮 -->
        <el-button
          type="warning"
          icon="el-icon-refresh-left"
          @click="resetFilters"
          class="reset-button"
        >
          重置
        </el-button>
      </div>

      <!-- 时间线显示博客列表 -->
      <el-timeline>
        <el-timeline-item
          v-for="blog in blogs"
          :key="blog.id"
          :timestamp="blog.created"
          placement="top"
        >
          <el-card class="blog-card">
            <h4>
              <router-link :to="{ name: 'BlogDetail', params: { blogId: blog.id } }">
                {{ blog.title }}
              </router-link>
            </h4>
            <p>{{ blog.description }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="page"
        v-if="!searchMode"
      />
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";

export default {
  name: "Blogs",
  components: { Header },
  data() {
    return {
      blogs: [],
      currentPage: 1,
      total: 0,
      pageSize: 5,
      searchQuery: "",
      searchMode: false,
      searchTimeout: null,
      categories: [],
      tags: [],
      selectedCategory: null,
      selectedTag: null
    };
  },
  methods: {
    // 分页加载
    page(currentPage) {
      this.$axios
        .get("/blogs", {
          params: {
            currentPage,
            pageSize: this.pageSize
          }
        })
        .then((res) => {
          if (res.data.code === 200) {
            this.blogs = res.data.data.records;
            this.currentPage = res.data.data.current;
            this.total = res.data.data.total;
            this.pageSize = res.data.data.size;
            this.searchMode = false;
          }
        })
        .catch((error) => {
          console.error("Failed to fetch blogs:", error);
        });
    },
    resetFilters() {
      this.searchQuery = '';
      this.selectedCategory = null;
      this.selectedTag = null;
      this.searchMode = false;
      this.page(1); // 重新加载分页数据
    },
    // 组合搜索函数
    handleSearch() {
  clearTimeout(this.searchTimeout);
  this.searchTimeout = setTimeout(() => {
    if (
      !this.searchQuery.trim() &&
      !this.selectedCategory &&
      !this.selectedTag
    ) {
      this.searchMode = false;
      this.page(1); // 重新加载分页数据
      return;
    }

    this.searchMode = true;

    const queryParams = {};
    if (this.searchQuery.trim()) {
      queryParams.query = this.searchQuery.trim();
    }
    if (this.selectedCategory !== null) {  // 确保分类已选
      queryParams.categoryId = this.selectedCategory;
    }
    if (this.selectedTag !== null) {  // 确保标签已选
      queryParams.tagId = this.selectedTag;
    }

    this.$axios
      .get("/blogs/search", { params: queryParams })
      .then((res) => {
        if (res.data.code === 200) {
          this.blogs = res.data.data;
        }
      })
      .catch((error) => {
        console.error("Failed to search blogs:", error);
      });
  }, 300);
},
    // 获取分类和标签数据
    fetchMetaData() {
      this.$axios.get("/categories").then((res) => {
        this.categories = res.data.data;
      });
      this.$axios.get("/tags").then((res) => {
        this.tags = res.data.data;
      });
    }
  },
  created() {
    if (this.$store.state.userInfo) {
      this.page(1);
    }
    this.fetchMetaData();
  },
  beforeUnmount() {
    clearTimeout(this.searchTimeout);
  }
};
</script>

<style scoped>
.blogs-container {
  padding-top: 30px;  /* 给博客页面增加顶部间隙 */
}
.mcontainer {
  font-family: 'Arial', sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
}

.main-content {
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding-left: 40px;
  gap: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 10px;
}

.search-input,
.select-box {
  width: 300px;
}

.search-button,
.reset-button {
  height: 38px;
  font-size: 16px;
}

.blog-card {
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  background-color: #f7f9fc;
  margin-bottom: 15px;
}

.blog-card h4 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.blog-card p {
  color: #777;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
  padding: 10px 0;
}

.el-avatar {
  margin-bottom: 20px;
}

.el-link {
  color: #409eff;
  text-decoration: none;
}

.el-link:hover {
  text-decoration: underline;
}

.maction {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  gap: 20px;
}

.el-divider {
  margin: 0;
}
</style>
