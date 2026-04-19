# HR-Agent 智能人力资源管理系统

## 项目概述

HR-Agent是一个基于Spring Boot 3.3.13和Java 21的智能人力资源管理系统，集成了AI能力（通义千问和DeepSeek大模型），提供简历管理、面试流程、Offer发放等全流程HR管理功能。系统采用微服务架构，支持动态配置、监控告警和高并发处理。

## 系统架构

### 技术栈概览

| 组件 | 技术选型 | 版本 |
|------|----------|------|
| 后端框架 | Spring Boot | 3.3.13 |
| AI框架 | Spring AI Alibaba | 1.0.0.3 |
| ORM框架 | MyBatis Plus | 3.5.11 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 6.0+ |
| 配置中心 | Nacos | 2023.0.3.3 |
| 监控 | Prometheus + Actuator | - |
| 线程池 | DynamicTp | 1.2.2-x |
| 工具库 | Hutool, PDFBox | 5.8.38, 2.0.30 |

### 项目结构

```
src/main/java/com/ai/hragent/
├── config/                    # 配置类
│   ├── ChatClientConfig.java     # AI客户端配置
│   ├── CorsConfig.java           # 跨域配置
│   ├── DashScopeConfig.java      # 百炼平台配置
│   ├── PageConfig.java           # 分页配置
│   └── RedisMemoryConfig.java    # Redis内存配置
├── controller/                # 控制器层
│   ├── AiController.java         # AI智能对话
│   ├── InterviewController.java  # 面试管理
│   ├── OfferController.java      # Offer管理
│   ├── ResumeController.java     # 简历管理
│   ├── RoleController.java       # 角色管理
│   ├── UserController.java       # 用户管理
│   └── UserRoleController.java   # 用户角色管理
├── entity/                    # 实体类
│   ├── Interview.java           # 面试实体
│   ├── Offer.java              # Offer实体
│   ├── Resume.java             # 简历实体
│   ├── Role.java               # 角色实体
│   ├── User.java               # 用户实体
│   └── UserRole.java           # 用户角色关联实体
├── mapper/                    # 数据访问层
│   ├── InterviewMapper.java     # 面试Mapper
│   ├── OfferMapper.java        # Offer Mapper
│   ├── ResumeMapper.java       # 简历Mapper
│   ├── RoleMapper.java         # 角色Mapper
│   ├── UserMapper.java         # 用户Mapper
│   └── UserRoleMapper.java     # 用户角色Mapper
├── service/                   # 服务接口层
│   ├── IInterviewService.java   # 面试服务接口
│   ├── IOfferService.java      # Offer服务接口
│   ├── IResumeService.java     # 简历服务接口
│   ├── IRoleService.java       # 角色服务接口
│   ├── IUserRoleService.java   # 用户角色服务接口
│   └── IUserService.java       # 用户服务接口
├── service/impl/              # 服务实现层
│   ├── InterviewServiceImpl.java
│   ├── OfferServiceImpl.java
│   ├── ResumeServiceImpl.java
│   ├── RoleServiceImpl.java
│   ├── UserRoleServiceImpl.java
│   └── UserServiceImpl.java
└── tools/                     # 工具类
    └── Result.java            # 统一返回结果封装
```

## 核心功能模块详解

### 1. AI智能对话模块 (AiController)

#### 功能特性
- **意图识别**：通过AI识别用户意图，支持四种类型：
  - 生成招聘信息
  - 公司规章制度查询（RAG检索增强）
  - 简历入库（PDF解析与处理）
  - 普通聊天对话
- **多模型支持**：同时集成通义千问(Qwen)和DeepSeek两种大模型
- **会话记忆**：基于Redis存储对话历史，支持连续对话
- **动态线程池**：使用DynamicTp进行并发任务处理

#### 关键API接口

| 端点 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/aichat/sendmsg` | POST | AI对话入口 | `AiMsg`对象 |
| `/aichat/upload` | POST | PDF文件上传 | `MultipartFile` |
| `/aichat/getmessages` | GET | 获取历史会话 | `accesstoken` |
| `/aichat/models` | GET | 获取可用模型列表 | 无 |
| `/aichat/dtp` | GET | 动态线程池测试 | 无 |

#### 数据模型
```java
// AI消息对象
record AiMsg(String content, List<MyFile> files, String model) {}

// 文件对象
record MyFile(String id, String name, String url) {}

// 模型对象
record Model(String id, String name, boolean available) {}
```

#### 简历处理流程
1. **PDF上传** → 文件存储到本地目录
2. **并发解析** → 使用动态线程池并行提取信息
3. **信息提取** → 正则表达式匹配关键字段
4. **智能筛选** → 学历验证等规则过滤
5. **数据存储** → 保存到数据库

### 2. 简历管理模块 (ResumeController)

#### 简历状态流转
```
-3 = 拒绝Offer
-2 = 面试未通过
-1 = 简历未通过筛选
 1 = 待处理
 2 = 通过筛选/面试中/一面
 3 = 二面
 4 = 三面
 5 = 待发Offer
 6 = 已发Offer/入职
```

#### API接口
| 端点 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/resume/list` | POST | 简历列表查询 | `ResumeListParams` |
| `/resume/statistics` | GET | 简历统计信息 | 无 |
| `/resume/add` | POST | 手动新增简历 | `Resume`对象 |
| `/resume/update` | POST | 修改简历 | `Resume`对象 |
| `/resume/delete` | POST | 删除简历 | `Resume`对象 |
| `/resume/deletes` | POST | 批量删除 | `IdsParams` |

### 3. 面试管理模块 (InterviewController)

#### 面试评分体系
- **专业得分** (professionalScore)：专业技能评估
- **沟通得分** (communicationScore)：沟通能力评估
- **团队协作得分** (teamworkScore)：团队合作能力
- **综合得分** (comprehensiveScore)：总体评价

#### 事务处理流程
```java
@Transactional
@RequestMapping("/add")
public Result add(@RequestBody Interview interview) {
    // 1. 保存面试评价
    // 2. 更新简历冗余字段（面试官、面试时间）
    // 3. 状态一致性保证
}
```

#### API接口
| 端点 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/interview/statistics` | GET | 面试统计信息 | 无 |
| `/interview/add` | POST | 新增面试记录 | `Interview`对象 |
| `/interview/update` | POST | 更新面试评价 | `Interview`对象 |
| `/interview/getlistbyrid` | GET | 按简历ID查询 | `rid`参数 |

### 4. Offer管理模块 (OfferController)

#### Offer状态管理
- **5** = 待发Offer
- **6** = 已发Offer
- **-3** = 拒绝Offer

#### AI邮件生成流程
1. **模板加载** → 从`prompt-template/offer-prompt`加载
2. **变量替换** → 姓名、部门、职位、薪资等
3. **AI生成** → 调用通义千问生成个性化邮件
4. **邮件发送** → 通过SMTP发送给候选人

#### 事务一致性
```java
@Transactional
@RequestMapping("/sendemail")
public Result sendEmail(@RequestBody Offer offer) {
    // 1. 验证Offer状态
    // 2. AI生成邮件内容
    // 3. 发送邮件
    // 4. 更新Offer表状态
    // 5. 更新Resume表状态
    // 6. 异常回滚机制
}
```

### 5. 用户管理模块 (UserController)

#### JWT认证流程
1. **用户登录** → 验证用户名密码
2. **Token生成** → 使用Hutool JWT生成token
3. **Token验证** → 接口权限校验
4. **用户信息** → 从token解析用户信息

#### API接口
| 端点 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/login` | POST | 用户登录 | `User`对象 |
| `/userinfo` | POST | 获取用户信息 | `accessToken` |
| `/logout` | GET | 退出登录 | 无 |
| `/user/getlist` | POST | 用户列表查询 | 分页参数 |
| `/user/delete` | POST | 删除用户 | `ids`数组 |
| `/user/edit` | POST | 新增/修改用户 | `User`对象 |

## 数据流转完整链路

### 1. 简历入库全流程

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  前端上传    │───▶│ 文件存储    │───▶│ PDF解析     │───▶│ 信息提取    │
│  PDF简历    │    │ (本地目录)  │    │ (PDFBox)    │    │ (正则匹配)  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                        │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ▼
│  智能筛选    │◀───│ 并发处理    │◀───│ 字段验证    │    ┌─────────────┐
│ (学历验证)   │    │ (动态线程池)│    │ (数据清洗)  │───▶│ 数据库存储  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**详细步骤：**
1. **前端上传**：用户通过Web界面上传PDF简历文件
2. **文件存储**：文件保存到`D:\imgs\data\`目录，使用UUID重命名
3. **PDF解析**：使用Apache PDFBox提取文本内容
4. **并发信息提取**：使用动态线程池并行提取：
   - 姓名：正则匹配`姓名[:：]\s*(\S{2,4})`
   - 手机号：正则匹配`1[3-9]\d{9}`
   - 邮箱：正则匹配`\w+@\w+(\.\w+)+`
   - 学历：正则匹配`(?:学历|教育经历)[:：]\s*(\S+)`
   - 年龄：正则匹配`年龄[:：]\s*(\d{1,2})`
   - 工作年限：正则匹配`工作年限[:：]\s*(\d+(\.\d+)?)`
5. **智能筛选**：根据业务规则过滤（如：专科/大专学历自动拒绝）
6. **数据存储**：保存到MySQL数据库的`resume`表

### 2. 面试流程数据流转

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  简历筛选    │───▶│ 安排面试    │───▶│ 面试评价    │───▶│ 结果处理    │
│ (state=1)   │    │ (state=2)   │    │ (评分记录)  │    │ (状态更新)  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                  │                  │                  │
        ▼                  ▼                  ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Resume表   │    │  Interview  │    │  Interview  │    │  Resume表   │
│  状态更新    │    │  表新增记录 │    │  表更新评价 │    │  状态流转   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**面试状态流转规则：**
- 一面通过：`state`从2变为3
- 二面通过：`state`从3变为4  
- 三面通过：`state`从4变为5（进入Offer待发状态）
- 面试失败：`state`变为-2

### 3. Offer发放数据流转

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  面试通过    │───▶│ 生成Offer   │───▶│ AI邮件生成  │───▶│ 邮件发送    │
│ (state=5)   │    │ 数据准备    │    │ (通义千问)  │    │ (SMTP)      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                  │                  │                  │
        ▼                  ▼                  ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Offer表    │    │  Prompt     │    │  邮件内容   │    │  状态更新   │
│  新增记录    │    │  模板渲染   │    │  生成完成   │    │ (state=6)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**事务一致性保证：**
```java
// Offer发送事务
@Transactional
public Result sendEmail(@RequestBody Offer offer) {
    // 1. 验证状态
    if (offer.getStatus() != 5) return Result.fail(-2, "无效状态");
    
    // 2. AI生成邮件
    String emailContent = qwenChatClient.prompt(prompt).call().content();
    
    // 3. 发送邮件
    mailSender.send(message);
    
    // 4. 更新Offer状态
    offer.setStatus(6);
    offerService.saveOrUpdate(offer);
    
    // 5. 更新Resume状态
    resume.setState(6);
    resumeService.saveOrUpdate(resume);
    
    // 6. 异常回滚
    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
}
```

### 4. AI对话数据流转

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  用户输入    │───▶│ 意图识别    │───▶│ 路由分发    │───▶│ 业务处理    │
│  (消息内容)  │    │ (AI判断)    │    │ (Switch分支)│    │ (对应服务)  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                  │                  │                  │
        ▼                  ▼                  ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  消息接收    │    │  Prompt     │    │  路由逻辑   │    │  结果返回   │
│  (Controller)│    │  模板匹配   │    │  执行       │    │  给用户     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**意图识别流程：**
1. **加载模板**：从`prompt-template/intention-prompt`加载意图识别模板
2. **AI判断**：调用通义千问进行意图分类
3. **路由分发**：
   - "生成招聘信息" → `chat()`方法
   - "公司规章制度查询" → `rules()`方法（RAG检索）
   - "简历入库" → `doResume()`方法
   - 默认 → `chat()`方法

### 5. 系统监控数据流转

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  应用指标    │───▶│ Actuator    │───▶│ Prometheus  │───▶│ 监控告警    │
│  (Metrics)  │    │  端点暴露    │    │  数据采集    │    │  (Alert)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        ▼                  ▼                  ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  动态线程池  │    │  HTTP端点   │    │  时序数据库  │    │  告警规则   │
│  指标采集    │    │  /actuator  │    │  存储指标    │    │  触发通知   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘


## 总结

HR-Agent系统通过AI技术实现了人力资源管理的智能化，主要特点包括：

1. **智能化处理**：AI驱动的简历解析、意图识别、邮件生成
2. **全流程管理**：从简历投递到入职的全流程跟踪
3. **高性能架构**：动态线程池、Redis缓存、异步处理
4. **可扩展性**：微服务架构、配置中心、监控体系
5. **易维护性**：清晰的代码结构、完善的文档、监控告警

系统适用于中小型企业的HR部门，能够显著提升招聘效率，减少人工工作量，实现数据驱动的决策支持。

## 前端 README

[此处添加前端项目的详细说明，包括技术栈、运行方式、项目结构等。]

### 技术栈

- **框架**: Vue.js / React / Angular (选择其中一个，或列出实际使用的框架)
- **UI 组件库**: Element UI / Ant Design / Material-UI (根据实际情况填写)
- **状态管理**: Vuex / Redux / Zustand (根据实际情况填写)
- **构建工具**: Vite / Webpack (根据实际情况填写)
- **包管理**: npm / yarn

### 项目结构

```
frontend/
├── public/             # 静态资源
├── src/
│   ├── assets/         # 静态文件、图片等
│   ├── components/     # 可复用组件
│   ├── views/          # 页面组件
│   ├── router/         # 路由配置
│   ├── store/          # 状态管理（如Vuex/Redux模块）
│   ├── utils/          # 工具函数
│   ├── App.vue         # 根组件 (Vue)
│   ├── main.js         # 应用入口文件
│   └── styles/         # 全局样式
├── .env                # 环境变量配置
├── package.json        # 项目依赖及脚本
└── README.md           # 前端项目自身的说明
```

### 运行方式

1.  **安装依赖**：
    ```bash
    cd frontend
    npm install  # 或 yarn
    ```
2.  **启动开发服务器**：
    ```bash
    npm run dev  # 或 yarn dev
    ```
3.  **构建生产版本**：
    ```bash
    npm run build  # 或 yarn build
    ```

### 架构设计

前端项目采用模块化和组件化的开发模式，主要特点如下：

-   **组件化开发**：将页面拆分为独立、可复用的组件，提高开发效率和可维护性。
-   **路由管理**：使用Vue Router/React Router等进行页面路由管理，实现单页应用（SPA）体验。
-   **状态管理**：通过Vuex/Redux等集中管理应用状态，确保数据流清晰可控。
-   **API 服务封装**：统一封装后端 API 请求，便于管理和维护。
-   **响应式布局**：兼容不同设备和屏幕尺寸，提供良好的用户体验。
