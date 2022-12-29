create table if not exists data_load_job_info(
   job_id varchar(40) not null
   ,tenant varchar(50) not null
  ,data_name    varchar(40) not null
  ,data_definition    text not null
  ,started_millis       bigint not null
  ,chunk_name varchar(200) not null
  ,status    varchar (50)
  ,error_msg    varchar (250)
  ,ended_millis bigint
  ,user_id varchar(100)
);

create index if not exists "data_load_job_info_jobid" on data_load_job_info using btree (job_id);
create index if not exists "data_load_job_info_tenant" on data_load_job_info using btree (tenant);

create table if not exists api_user(
   id varchar(100) not null primary key,
   tenant varchar(100) not null,
   object_storage_id varchar(100) not null,
   client_id varchar(100) not null,
   api_key varchar(100) not null,
   active boolean not null default true
);

create table if not exists data_load_job_errors(
   job_id varchar(40) not null,
   data_name varchar(40) not null
   ,tenant varchar(50) not null
  ,rowJson    text not null
  ,error_msgs   VARCHAR [] not null
  ,chunk_name varchar(200) not null
);

create index if not exists "data_load_job_errors_jobid" on data_load_job_errors using btree (job_id);


insert into api_user(id, tenant, object_storage_id, client_id, api_key) values ('taxreco@gmail.com', 'public', 'taxreco', '6c3063e1-60bd-4b08-934f-b66d47685544', 'd877770c-41f8-46db-bcef-78d06c5ec0e1');
create index if not exists "api_user_apikey_client_id" on api_user using btree (client_id, api_key);

