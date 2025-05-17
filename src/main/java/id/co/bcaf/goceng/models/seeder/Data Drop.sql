DECLARE @sql NVARCHAR(MAX) = N'';

-- Step 1: Drop all foreign key constraints
SELECT @sql += 'ALTER TABLE [' + OBJECT_SCHEMA_NAME(parent_object_id) + '].[' + OBJECT_NAME(parent_object_id) + '] DROP CONSTRAINT [' + name + '];' + CHAR(13)
FROM sys.foreign_keys;

-- Step 2: Drop all tables
SELECT @sql += 'DROP TABLE [' + SCHEMA_NAME(schema_id) + '].[' + name + '];' + CHAR(13)
FROM sys.tables;

-- Execute the combined SQL
EXEC sp_executesql @sql;
