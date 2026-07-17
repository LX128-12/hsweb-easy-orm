# Kingbase 兼容模式测试覆盖报告

## 三种兼容模式：Oracle / MSSQL / MySQL

| 模式 | 单元测试 | 集成测试 | 合计 |
|---|---|---|---|
| Oracle | 15 | 54 | 69 |
| MSSQL | 21 | 41 | 62 |
| MySQL | 13 | 39 | 52 |
| **总计** | **49** | **168** | **217** |

---

## 一、DDL（数据定义）

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| 建表 SQL（基本列类型） | 单元 | ✅ | ✅ | ✅ |
| 建表 SQL（复合主键） | 单元 | ✅ | ✅ | ✅ |
| 建表 SQL（多索引） | 单元 | ✅ | ✅ | ✅ |
| 建表 SQL（无表注释） | 单元 | ✅ | ✅ | ✅ |
| 建表 SQL（CLOB / NUMBER 精度） | 单元 | ✅ | — | — |
| 建表 SQL（NVARCHAR / BOOLEAN） | 单元 | — | ✅ | — |
| 建表 SQL（无自增列） | 单元 | — | — | ✅ |
| 建表 + 删表 | 集成 | ✅ | ✅ | ✅ |
| DDL Alter（增列 / 改列 / 删列） | 集成 | ✅ | ✅ | ✅ |
| AlterTableSqlBuilder（注释清除 + 注释添加） | 单元 | — | ✅ | — |
| 列重命名（RENAME COLUMN） | 集成 | ✅ | ✅ | ✅ |
| 表重命名（RENAME TO） | 集成 | ✅ | ✅ | ✅ |
| 唯一约束（ADD / DROP CONSTRAINT） | 集成 | ✅ | — | — |
| 创建索引（普通 / 唯一 / 复合 / ASC / DESC） | 单元 | ✅ | ✅ | ✅ |
| 索引元数据解析 | 集成 | ✅ | ✅ | ✅ |

---

## 二、DML（数据操作）

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| 单行 Insert | 集成 | ✅ | ✅ | ✅ |
| 多行 Insert | 集成 | ✅ | ✅ | ✅ |
| Update | 集成 | ✅ | ✅ | ✅ |
| Delete | 集成 | ✅ | ✅ | ✅ |
| Upsert（INSERT OR UPDATE） | 集成 | ✅ | ✅ | ✅ |
| 重复主键异常 | 集成 | ✅ | ✅ | ✅ |

---

## 三、DQL（数据查询）

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| 聚合 COUNT / SUM / AVG / MAX / MIN | 集成 | ✅ | ✅ | ✅ |
| GROUP BY | 集成 | ✅ | ✅ | ✅ |
| LIKE（前缀 / 后缀 / 包含 / 精确） | 集成 | ✅ | ✅ | ✅ |
| IN / NOT IN | 集成 | ✅ | ✅ | ✅ |
| IS NULL / NOT NULL | 集成 | ✅ | ✅ | ✅ |
| NULL 值读写往返 | 集成 | ✅ | ✅ | ✅ |
| 大文本 10KB 读写往返 | 集成 | ✅ | ✅ | ✅ |
| ORDER BY ASC / DESC（多列） | 集成 | ✅ | ✅ | ✅ |
| OR 条件组合 | 集成 | ✅ | ✅ | ✅ |
| LEFT JOIN | 集成 | ✅ | ✅ | ✅ |
| 子查询 | 集成 | ✅ | ✅ | ✅ |
| 分页首页 | 集成 | ✅ | ✅ | ✅ |
| 分页末页（边界） | 集成 | ✅ | ✅ | ✅ |
| 分页空表（边界） | 集成 | ✅ | ✅ | ✅ |
| 枚举值 IN 查询 | 集成 | ✅ | ✅ | ✅ |

---

## 四、事务

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| Commit 可见性 | 集成 | ✅ | ✅ | ✅ |
| Rollback 不可见 | 集成 | ✅ | ✅ | ✅ |
| Savepoint 回滚 | 集成 | ✅ | ✅ | ✅ |
| Savepoint 释放 | 集成 | ✅ | ✅ | ✅ |
| 嵌套 Savepoint | 集成 | ✅ | — | — |
| ALTER COLUMN TYPE 后数据完整性 | 集成 | ✅ | — | — |

---

## 五、外键

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| 外键约束 + JOIN 查询 | 集成 | ✅ | ✅ | ✅ |
| 级联删除（ON DELETE CASCADE） | 集成 | ✅ | — | — |

---

## 六、特殊数据类型

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| DECIMAL(18,4) 精度往返 | 集成 | ✅ | ✅ | ✅ |
| BOOLEAN 真 / 假往返 | 集成 | ✅ | ✅ | ✅ |
| CLOB 5000 字符往返 | 集成 | ✅ | — | — |
| TIMESTAMP 往返 | 集成 | ✅ | ✅ | ✅ |
| VARCHAR(2048) 长字符串往返 | 集成 | — | ✅ | — |
| JSONB 往返 | 集成 | — | — | ✅ |

---

## 七、Schema 元数据

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| parseAll 列出所有表 | 集成 | ✅ | ✅ | ✅ |
| parseByName 解析表结构 | 集成 | ✅ | ✅ | ✅ |
| getTableExistsSql 模板 | 集成 | ✅ | ✅ | — |
| 表注释写入 + 读取 | 集成 | ✅ | ✅ | ✅ |
| 列注释写入 + 读取 | 集成 | ✅ | ✅ | ✅ |

---

## 八、异常翻译

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| DuplicateKeyException 翻译 | 单元 + 集成 | ✅ | ✅ | ✅ |
| 表不存在 | 集成 | ✅ | ✅ | ✅ |
| 列不存在 | 集成 | ✅ | ✅ | ✅ |
| 语法错误 | 集成 | ✅ | ✅ | ✅ |
| NOT NULL 违反 | 集成 | ✅ | ✅ | — |
| 数据类型不匹配 | 集成 | ✅ | — | — |
| 非 SQL 异常透传 | 单元 | ✅ | ✅ | ✅ |

---

## 九、Dialect 特性

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| getId / getName 标识 | 单元 | ✅ | ✅ | ✅ |
| 引号格式（quote） | 单元 | ✅ | ✅ | ✅ |
| isColumnToUpperCase | 单元 | ✅ | — | ✅ |
| 类型转换（继承父类类型） | 单元 | ✅ | ✅ | ✅ |
| 类型转换（PG 原生类型注册） | 单元 | — | ✅ | ✅ |

---

## 十、默认值

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| sysdate / now() 时间默认值 | 集成 | ✅ | — | ✅ |
| 数值默认值（default 0） | 集成 | ✅ | ✅ | ✅ |

---

## 十一、Oracle 专有

| 场景 | 覆盖方式 |
|---|---|
| SEQUENCE + nextval 自增 ID | 集成 |
| 显式 ID 插入（无序列） | 集成 |
| ALTER COLUMN TYPE 类型变更 | 集成 |

---

## 十二、TableMetadataParser

| 场景 | 覆盖方式 | Oracle | MSSQL | MySQL |
|---|---|---|---|---|
| applyColumnInfo（长度 / 精度 / 刻度补充） | 单元 | ✅ | ✅ | ✅ |
| applyColumnInfo（长度默认 255） | 单元 | — | ✅ | — |

---

## 连接配置

测试通过系统属性或环境变量配置 Kingbase 连接，支持模式专用配置和通用回退配置：

| 模式 | 系统属性前缀 | 环境变量前缀 | 默认端口 |
|---|---|---|---|
| Oracle | `kingbase.oracle.*` | `KINGBASE_ORACLE_*` | 54322 |
| MSSQL | `kingbase.mssql.*` | `KINGBASE_MSSQL_*` | 54323 |
| MySQL | `kingbase.mysql.*` | `KINGBASE_MYSQL_*` | 54321 |
| 通用回退 | `kingbase.*` | `KINGBASE_*` | — |

必要参数：`url`、`username`、`password`；可选参数：`schema`（默认 `public`）、`driver`（默认 `com.kingbase8.Driver`）。

### 运行命令示例

```bash
# 运行 Oracle 模式全部集成测试
mvn test -pl hsweb-easy-orm-rdb \
  -Dkingbase.oracle.url="jdbc:kingbase8://localhost:54322/hsweb4" \
  -Dkingbase.oracle.username="system" \
  -Dkingbase.oracle.password="1234" \
  -Dtest="KingbaseOracleBasicTest,KingbaseOracleExtendedQueryTest,..."

# 运行全部单元测试（无需数据库连接）
mvn test -pl hsweb-easy-orm-rdb \
  -Dtest="KingbaseOracleCreateIndexSqlBuilderTest,KingbaseOracleCreateTableSqlBuilderTest,..."
```
