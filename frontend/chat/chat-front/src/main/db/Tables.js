const add_tables = [
    "CREATE TABLE if not exists chat_message(" +
    " user_id varchar not null," +
    " message_id bigint NOT NULL default null," +
    " session_id varchar," +
    " message_type integer," +
    " message_content varchar," +
    " contact_type integer," +
    " send_user_id varchar," +
    " send_user_nick_name varchar," +
    " send_time bigint," +
    " status integer," +
    " file_size bigint," +
    " file_name varchar," +
    " file_path varchar," +
    " file_type integer," +
    " PRIMARY KEY (message_id, user_id)" +
    ");",
    "CREATE TABLE if not exists chat_session_user(" + 
    " user_id varchar not null default 0," +
    " contact_id varchar(11) not null," +
    " contact_type integer," +
    " session_id varchar(11)," +
    " status integer default 1," +
    " contact_name varchar(20)," +
    " last_message varchar(500)," +
    " last_receive_time bigint," +
    " no_read_count integer default 0," +
    " member_count integer," +
    " top_type integer default 0," +
    " PRIMARY KEY (user_id, contact_id)" +
    ");",
    "CREATE TABLE if not exists user_setting(" +
    " user_id varchar not null," + 
    " email varchar not null," +
    " sys_setting varchar," +
    " contact_no_read integer," +
    " server_port integer," +
    " PRIMARY KEY (user_id)" +
    ");"
]

const alter_tables = [

]

const add_index = [
    "create index if not exists idx_session_id" +
    " on chat_message(" +
    " session_id asc" +
    ");"
]

export {
    add_index,
    add_tables,
    alter_tables
}