<template>
  <div class="mcontaner">
    <Header />

    <div class="block">
      <!-- 搜索栏 + 分类 + 标签 -->
      <div class="search-container">
        <!-- 搜索输入框 -->
        <el-input
          v-model="searchQuery"
          placeholder="🔍 搜索博客标题..."
          clearable
          @keyup.enter="handleSearch"
          style="width: 300px; margin-right: 10px"
        ></el-input>

        <!-- 搜索按钮 -->
        <el-button 
          type="primary" 
          icon="el-icon-search" 
          @click="handleSearch"
          style="margin-right: 20px"
        >
          搜索
        </el-button>

        <!-- 分类下拉 -->
        <el-select
          v-model="selectedCategory"
          placeholder="📂 分类"
          @change="handleSearch"
          clearable
          style="width: 150px; margin-right: 10px"
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
          style="width: 150px"
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
          <el-card>
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
        class="mpage"
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
    page(pageNumber) {
  if (this.searchMode) {
    this.handleSearch(pageNumber);
  } else {
    this.$axios
      .get("/blogs", {
        params: {
          currentPage: pageNumber,
          pageSize: this.pageSize
        }
      })
      .then((res) => {
        if (res.data.code === 200) {
          this.blogs = res.data.data.records;
          this.currentPage = res.data.data.current;
          this.total = res.data.data.total;
          this.pageSize = res.data.data.size;
        }
      })
      .catch((error) => {
        console.error("分页获取失败:", error);
      });
  }
},
    resetFilters() {
  this.searchQuery = '';
  this.selectedCategory = null;
  this.selectedTag = null;
  this.searchMode = false;
  this.page(1); // 重新加载分页数据
},
    // 组合搜索函数
    handleSearch(page = 1) {
  clearTimeout(this.searchTimeout);
  this.searchTimeout = setTimeout(() => {
    // 没条件就走分页
    if (
      !this.searchQuery.trim() &&
      !this.selectedCategory &&
      !this.selectedTag
    ) {
      this.searchMode = false;
      this.page(1); 
      return;
    }

    this.searchMode = true;

    const queryParams = {
      currentPage: page,
      pageSize: this.pageSize,
    };
    if (this.searchQuery.trim()) {
      queryParams.query = this.searchQuery.trim();
    }
    if (this.selectedCategory) {
      queryParams.categoryId = this.selectedCategory;
    }
    if (this.selectedTag) {
      queryParams.tagId = this.selectedTag;
    }

    this.$axios
      .get("/blogs/search", { params: queryParams })
      .then((res) => {
        if (res.data.code === 200) {
          this.blogs = res.data.data.records;
          this.currentPage = res.data.data.current;
          this.total = res.data.data.total;
          this.pageSize = res.data.data.size;
        }
      })
      .catch((error) => {
        console.error("搜索失败:", error);
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
.mpage {
  margin: 0 auto;
  text-align: center;
}

.search-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding-left: 40px;
  gap: 10px;
}

.el-timeline {
  padding-left: 40px;
}
</style>
