<template>
  <div class="company-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="公司名称">
          <el-input 
            v-model="searchForm.companyName" 
            placeholder="请输入公司名称" 
            clearable 
            @clear="handleSearch"
            style="width: 200px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="公司状态">
          <el-select 
            v-model="searchForm.companyStatus" 
            placeholder="公司状态" 
            clearable
            @change="handleSearch"
            @clear="handleSearch"
            style="width: 150px"
          >
            <el-option label="存续中" :value="1" />
            <el-option label="已关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增公司
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="companyId" label="公司ID" width="100" />
        <el-table-column prop="companyName" label="公司名称" width="250" />
        <el-table-column prop="responsiblePerson" label="负责人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="companyStatus" label="公司状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.companyStatus === 1" type="success">存续中</el-tag>
            <el-tag v-else type="info">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePerson">
              <el-input v-model="form.responsiblePerson" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="公司状态" prop="companyStatus">
          <el-radio-group v-model="form.companyStatus">
            <el-radio :label="1">存续中</el-radio>
            <el-radio :label="0">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getCompanies, createCompany, updateCompany, deleteCompany } from '../../api/company'

// 搜索表单
const searchForm = reactive({
  companyName: '',
  companyStatus: null
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  companyId: null,
  companyName: '',
  responsiblePerson: '',
  contactPhone: '',
  companyStatus: 1
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  responsiblePerson: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  companyStatus: [{ required: true, message: '请选择公司状态', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      companyName: searchForm.companyName || undefined,
      companyStatus: searchForm.companyStatus !== null ? searchForm.companyStatus : undefined
    }
    
    const res = await getCompanies(params)
    const allData = res.data || []
    total.value = allData.length
    
    // 客户端分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = allData.slice(start, end)
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.companyName = ''
  searchForm.companyStatus = null
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增公司'
  Object.assign(form, {
    companyId: null,
    companyName: '',
    responsiblePerson: '',
    contactPhone: '',
    companyStatus: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑公司'
  Object.assign(form, {
    companyId: row.companyId,
    companyName: row.companyName,
    responsiblePerson: row.responsiblePerson,
    contactPhone: row.contactPhone,
    companyStatus: row.companyStatus
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公司吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCompany(row.companyId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.companyId) {
          await updateCompany(form.companyId, form)
          ElMessage.success('更新成功')
        } else {
          await createCompany(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.company-management {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}
</style>
