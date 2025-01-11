# Migration scripts

Scripts used for DB changes using Liquibase

### Naming convention

```
V{DATE}.{TICKET NUMBER} OR {SHORTENED TICKET DESCRIPTION}.{migration-iteration-number}--{descriptive-title}.sql
```

> ⚠️ The preferred one is the 'Ticket number' to include in the name!

### Date format

```
yyyymmdd
```

### Examples

**First Input Data:**

    * Date: 2024-12-22
    * Ticket number: SE-1234

**Result:**

```
V20241222.SE-1234.01--this-is-a-descriptive-title-of-the-first-migration-file-of-the-ticket.sql
V20241222.SE-1234.02--this-is-a-descriptive-title-of-the-second-migration-file-of-the-ticket.sql
```

**Second Input Data:**

    * Date: 2024-12-22
    * Ticket description: Add initial tables

**Result:**

```
V20241222.initial.01--add-initial-tables.sql
```
