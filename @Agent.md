# Kingbase Compatibility Context

## Purpose

This repository is being extended to support KingbaseES in its MySQL,
Oracle, and SQL Server compatibility modes. Future work must preserve the
distinction between the Kingbase JDBC protocol and the selected compatibility
mode:

- The JDBC driver is always `com.kingbase8.Driver`.
- The XA implementation is always `com.kingbase8.xa.KBXADataSource`.
- SQL, DDL, metadata, pagination, and parameter conventions are selected by
  the compatibility mode, not inferred from the JDBC URL.

The immediate implementation goals are:

1. Make metadata SQL correct for each compatibility mode.
2. Implement a reusable `XADataSourceUtil` for Kingbase and integrate it into
   hswebframework datasource/JTA creation.
3. Keep the Easy ORM dialect selection and hswebframework integration aligned.
4. Preserve reactive application behavior without claiming native R2DBC support
   where the Kingbase driver cannot provide it.

## Repository Layout

```text
hsweb-easy-orm/
  hsweb-easy-orm-core/       Core metadata, feature, query parameter APIs
  hsweb-easy-orm-rdb/        JDBC/R2DBC ORM and database dialects
    src/main/java/.../supports/
      mysql/ oracle/ mssql/ postgres/     Baseline dialect implementations
      kingbase/
        mysql/                         Kingbase MySQL compatibility mode
        oracle/                        Kingbase Oracle compatibility mode
        mssql/                         Kingbase SQL Server compatibility mode
    src/test/java/.../supports/kingbase/ Mode-specific unit and JDBC tests
  kingbase-test-coverage.md   Existing coverage inventory
```

The sibling repository `../hsweb-framework` owns the Spring Boot integration:

```text
hsweb-framework/
  hsweb-commons/hsweb-commons-crud/
    .../configuration/kingbase/        Easy ORM DialectProvider SPI adapters
    .../resources/META-INF/services/   DialectProvider registration
  hsweb-datasource/hsweb-datasource-api/
    DatabaseType.java                  Driver/XA class/type detection
  hsweb-datasource/hsweb-datasource-jta/
    AtomikosDataSourceConfig.java      XA datasource construction path
```

## Existing Easy ORM Design

Each Kingbase mode has a `Dialect`, `SchemaMetadata`, table metadata parser,
index metadata parser, DDL builders, and JDBC exception translator:

| Compatibility mode | Dialect ID | Schema metadata |
| --- | --- | --- |
| MySQL | `kingbase-mysql` | `KingbaseMysqlSchemaMetadata` |
| Oracle | `kingbase-oracle` | `KingbaseOracleSchemaMetadata` |
| SQL Server | `kingbase-mssql` | `KingbaseMssqlSchemaMetadata` |

`Kingbase*SchemaMetadata` is the feature-composition boundary. It installs the
mode's DDL builders, paginator, metadata parsers, dialect, and exception
translation. New functionality belongs in the mode implementation, then is
registered from that schema class.

`RDBTableMetadataParser` consumes four SQL statements per mode:

1. table columns;
2. table comment;
3. all table names;
4. table existence.

It requires stable aliases such as `name`, `data_type`, `data_length`,
`data_precision`, `data_scale`, `not_null`, `comment`, and `table_name`.
Index metadata is loaded separately through `IndexMetadataParser`.

## Metadata SQL Status

### MySQL mode

`KingbaseMysqlTableMetadataParser` currently inherits
`MysqlTableMetadataParser` and only repairs missing length/precision/scale.
The inherited SQL uses MySQL quoting (backticks), `column_comment`,
`table_comment`, and `column_type`. This has passed the current integration
tests, but it relies on Kingbase's MySQL-compatible information schema rather
than expressing a Kingbase-owned metadata contract.

### Oracle and SQL Server modes

`KingbaseOracleTableMetadataParser` and
`KingbaseMssqlTableMetadataParser` both query `information_schema` with nearly
identical PostgreSQL-style SQL. They deliberately return `''` for table and
column comments. They also do not project `primary_key`; the base parser can
only populate that field when the SQL returns it. Their index parsers use
Kingbase/PostgreSQL catalogs, so index-level primary-key information is still
separate from column metadata.

### Required direction

Metadata SQL must be treated as a Kingbase capability matrix, not copied from
the emulated database:

- Confirm the available catalog views and identifier casing on a real database
  for each compatibility mode and target Kingbase version.
- Return the base parser's required aliases and actual comments/primary-key
  information where available.
- Keep table, column, index, and constraint metadata mutually consistent.
- Test `parseByName`, `parseAll`, table existence, comments, composite primary
  keys, unique indexes, precision/scale, and quoted/mixed-case identifiers in
  all three modes.

There is no runtime compatibility-mode detection in Easy ORM. Applications
must configure the intended dialect explicitly; a `jdbc:kingbase8:` URL alone
cannot distinguish MySQL, Oracle, and SQL Server mode.

## hswebframework Integration Status

The CRUD module already exposes six `DialectProvider` SPI entries:

| Configuration name | Provider |
| --- | --- |
| `kingbase-mysql`, `kingbase_mysql` | Kingbase MySQL provider |
| `kingbase-oracle`, `kingbase_oracle` | Kingbase Oracle provider |
| `kingbase-sqlserver`, `kingbase_sqlserver` | Kingbase SQL Server provider |

The providers are registered through
`META-INF/services/org.hswebframework.web.crud.configuration.DialectProvider`
and create the corresponding Easy ORM schema metadata. `easyorm.dialect` must
use one of these names. Note that the SQL Server provider name is
`kingbase-sqlserver`, while the Easy ORM dialect ID is `kingbase-mssql`; this
is intentional but must remain documented and tested.

`DatabaseType.kingbase` already identifies `jdbc:kingbase8:` URLs and exposes
the correct driver and XA class. It represents the physical Kingbase database,
not a compatibility mode, so it must not select an Easy ORM dialect by itself.

## Reactive Runtime Constraint

The application programming model is reactive and the framework has a native
R2DBC path (`R2dbcReactiveSqlExecutor` / `DefaultR2dbcExecutor`). Kingbase does
not provide an official R2DBC driver. Historical PR #96/#97 nevertheless used
`r2dbc-postgresql` as a protocol-compatibility driver because Kingbase exposes
the PostgreSQL wire protocol. This is a compatibility strategy, not vendor
driver support, and requires real-database verification for every target
Kingbase version and compatibility mode.

That historical implementation combined PostgreSQL R2DBC execution with
Kingbase MySQL SQL syntax: MySQL identifier quoting, pagination, and upsert;
PostgreSQL R2DBC exception translation; PostgreSQL-style `COMMENT ON`/ALTER
where required; and explicit CASTs of information-schema numeric fields that
the PostgreSQL R2DBC driver returned as strings.

When this R2DBC compatibility path is enabled through hswebframework, it must
use PostgreSQL parameter syntax (`$1`, `$2`, ...) rather than the JDBC/MySQL
`?` binding convention. It needs a distinct execution configuration or dialect
provider from JDBC Kingbase MySQL. Do not use the PostgreSQL R2DBC driver
without that explicit configuration and compatibility testing.

The current Kingbase compatibility implementation on the active branch is
JDBC-based: its dialects, DDL, metadata parsers, integration tests, and future
XA support use the Kingbase JDBC driver. The PostgreSQL R2DBC route is an
optional, separately validated execution profile rather than a replacement for
JDBC/XA support.

The available fallback is `JdbcReactiveSqlExecutor`, which exposes `Mono` and
`Flux` around blocking JDBC. This is a compatibility bridge, not R2DBC:

- The current `select` implementation subscribes on `boundedElastic`.
- The current `update` and `execute` implementations call blocking JDBC without
  a scheduler boundary. They can block a WebFlux event-loop thread.
- JDBC connection/transaction binding and reactive transaction context have
  different propagation models. Thread changes can affect the bound connection
  and transaction semantics.

Kingbase support must therefore use an explicit blocking-I/O policy: execute
all JDBC connection acquisition, DML, DDL, and result reading on a bounded,
observable scheduler; size it against the JDBC pool; preserve cancellation and
connection cleanup; and document that database backpressure is bounded by the
JDBC bridge rather than by a non-blocking driver. Verify this with thread-name
or BlockHound tests for `select`, `update`, and `execute`, plus transactional
commit/rollback tests under scheduler switches.

XA is also a JDBC concern. Atomikos can coordinate `KBXADataSource` resources,
but that does not make the R2DBC transaction manager XA-capable. A Kingbase XA
flow must have an explicit transaction boundary and must be tested separately
from native R2DBC transaction behavior.

## XADataSourceUtil Target

There is no `XADataSourceUtil` in either repository today. Atomikos currently
creates the configured XA class reflectively in `AtomikosDataSourceConfig` and
copies `xa-properties` directly onto it.

The Kingbase 9.0.1 driver provides `com.kingbase8.xa.KBXADataSource`. Its base
class supports `setUrl`/`setURL`, which parses `jdbc:kingbase8:` URLs, and uses
the JavaBean credential property `user` (`setUser`), not `username`.

`XADataSourceUtil` should therefore have a narrow, testable contract:

1. Accept a JDBC URL and standard credentials/options without assuming an
   emulated database mode.
2. Create `KBXADataSource` and set URL, user, password, and supported driver
   properties with correct JavaBean names and type conversion.
3. Validate the URL and report invalid or unsupported properties clearly.
4. Be used by both default and dynamic Atomikos datasource paths, so those
   paths cannot drift.
5. Preserve explicit `xa-properties`; do not silently overwrite caller
   supplied driver-specific options.

At minimum, add tests for URL parsing, `username` to `user` normalization,
custom port/schema/options, invalid URLs, and an Atomikos XA connection test
against a real Kingbase instance. A multi-resource XA commit/rollback test is
needed before claiming XA support.

## Verification Matrix

For each mode, verify the following separately:

- Easy ORM: DDL, DML, query/pagination, exception translation, table/index
  metadata, comments, and identifier casing.
- hswebframework: `DialectProviders.lookup` for hyphen and underscore aliases,
  Spring Boot context startup with the selected `easyorm.dialect`, and schema
  creation through the provider.
- Datasource/JTA: URL classification, XA datasource property binding, single
  resource XA lifecycle, and distributed commit/rollback/recovery where the
  deployment uses Atomikos.
- Reactive fallback: no JDBC work on the event loop, cancellation/connection
  cleanup, scheduler capacity under load, and transaction behavior across the
  scheduler boundary.

Current test coverage is concentrated in `hsweb-easy-orm-rdb` and covers
ordinary JDBC mode tests. It does not yet prove XA configuration or distributed
transaction semantics, and the hsweb4 Spring Boot integration tests currently
focus on the MySQL compatibility mode.

## Change Order

1. Establish real-database metadata SQL fixtures for all three modes.
2. Correct parser SQL and add regression tests before changing DDL behavior.
3. Add and test `XADataSourceUtil` in hswebframework.
4. Make the JDBC reactive bridge safe for all operation types and validate its
   transaction behavior before enabling Kingbase in a WebFlux request path.
5. Wire the utility into Atomikos configuration and document the configuration
   property contract.
6. Add end-to-end Spring Boot tests for every dialect provider, JDBC bridge,
   and XA mode.
