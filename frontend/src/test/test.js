const localTempId = -BigInt(Date.now()) - BigInt(Math.floor(Math.random() * 1000000));
console.log("生成的 localTempId: ", typeof localTempId === "bigint"
);