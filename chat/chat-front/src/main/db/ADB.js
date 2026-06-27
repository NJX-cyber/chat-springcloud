const fs = require('fs');
const path = require('path');
const os = require('os');
const sqlite3 = require('sqlite3').verbose();

import { add_index, add_tables, alter_tables } from './Tables';

const NODE_ENV = process.env.NODE_ENV;

const userDir = os.homedir();

const dbFolder = path.join(
    userDir,
    NODE_ENV === 'development' ? '.chattest' : '.chat'
);

// 安全创建目录
fs.mkdirSync(dbFolder, { recursive: true });

const db = new sqlite3.Database(path.join(dbFolder, 'local.db'));

const globalFieldsMap = {};

const createTable = () => {
    return new Promise(async (resolve, reject) => {
        for (const table of add_tables) {
            await db.run(table);
        }

        for (const index of add_index) {
            await db.run(index);
        }

        for (const table of alter_tables) {
            const fieldList = await queryAll(`PRAGMA table_info(${table.name})`, []);
            const field = fieldList.some(item => item.name === table.field);
            if (!field) {
                await db.run(table.sql);
            }
        }
        resolve();
    })
};

const initTableColumnsMap = async () => {
    let sql = `select name from sqlite_master where type='table' and name != 'sqlite_sequence'`
    let tables = await queryAll(sql, []);
    for (let i = 0; i < tables.length; i++) {
        let sql = `PRAGMA table_info(${tables[i].name})`
        let fields = await queryAll(sql, []);
        let fieldMap = {};
        for (let j = 0; j < fields.length; j++) {
            fieldMap[toCamelCase(fields[j].name)] = fields[j].name;
        }
        globalFieldsMap[tables[i].name] = fieldMap;
    }
    // console.log(globalFieldsMap);
}

const convertDbToBizObj = (data) => {
    if (!data) {
        return null;
    }
    const bizData = {};
    for (let item in data) {
        bizData[toCamelCase(item)] = data[item];
    }
    return bizData;
}

const toCamelCase = (str) => {
    return str.replace(/_([a-z])/g, function (match, p1) {
        return String.fromCharCode(p1.charCodeAt(0) - 32);
    });
}

const queryAll = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.all(params, function (err, row) {
            if (err) {
                resolve([]);
            }
            row.forEach((item, index) => {
                row[index] = convertDbToBizObj(item);
            });
            console.log(`执行sql: ${sql}, params: ${params}`)
            stmt.finalize();
            resolve(row);
        });
    });
}

const queryOne = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.get(params, function (err, row) {
            if (err) {
                resolve({});
            }
            console.log(`执行sql: ${sql}, params: ${params}`)
            stmt.finalize();
            resolve(convertDbToBizObj(row));
        });
    });
}
const queryCount = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.get(params, function (err, row) {
            if (err) {
                return resolve(0);
            }
            console.log(`执行sql: ${sql}, params: ${params}`)
            stmt.finalize();
            return resolve(Object.values(row)[0]);
        });
    });
}

const run = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.run(params, function (err) {
            if (err) {
                console.log(`执行sql: ${sql}, params: ${params}, 改变的记录数为：${this.changes}`)
                resolve("操作数据库失败");
            }
            console.log(`执行sql: ${sql}, params: ${params}, 改变的记录数为：${this.changes}`)
            stmt.finalize();
            resolve(this.changes);
        });
    });
};

const insert = (sqlPrefix, tableName, data) => {
    const columnsMap = globalFieldsMap[tableName];
    const dbColumns = [];
    const params = [];
    for (let item in data) {
        if (data[item] !== undefined && columnsMap[item] !== undefined) {
            dbColumns.push(columnsMap[item]);
            params.push(data[item]);
        }
    }

    const prepare = "?".repeat(dbColumns.length).split("").join(",");
    const sql = `${sqlPrefix} ${tableName}(${dbColumns.join(",")}) VALUES (${prepare})`;
    return run(sql, params);
};

const insertOrReplace = (tableName, data) => {
    return insert("INSERT OR REPLACE INTO", tableName, data);
}

const insertOrIgnore = (tableName, data) => {
    return insert("INSERT OR IGNORE INTO", tableName, data);
}

const update = (tableName, data, paramData) => {
    const columnsMap = globalFieldsMap[tableName];
    const dbColumns = [];
    const params = [];
    const whereColumns = [];
    for (let item in data) {
        if (data[item] !== null && columnsMap[item] !== undefined) {
            dbColumns.push(`${columnsMap[item]} = ?`);
            params.push(data[item]);
        }
    }

    for (let item in paramData) {
        if (paramData[item] !== null && columnsMap[item] !== undefined) {
            whereColumns.push(`${columnsMap[item]} = ?`);
            params.push(paramData[item]);
        }
    }

    const sql = `update ${tableName} set ${dbColumns.join(",")} ${whereColumns.length > 0 ? ' where ' : ''}${whereColumns.join(" and ")}`;
    return run(sql, params);

};

const init = () => {
    db.serialize(async () => {
        await createTable();
        await initTableColumnsMap();
    });
};

init();

export {
    createTable,
    run,
    queryAll,
    queryOne,
    queryCount,
    insertOrReplace,
    insertOrIgnore,
    update,
    insert
};