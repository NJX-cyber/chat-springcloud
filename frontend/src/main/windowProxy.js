const windowManage = {};

const saveWindow = (id, win) => {

    windowManage[id] = win;

    win.on("closed", () => {
        delete windowManage[id];
    });
};

const getWindow = (id) => {
    return windowManage[id];
};

const delWindow = (id) => {
    const win = getWindow(id);
    if (!win) return;
    if (win.isDestroyed()) return;
    win.close();
};

export {
    saveWindow,
    getWindow,
    delWindow,
};