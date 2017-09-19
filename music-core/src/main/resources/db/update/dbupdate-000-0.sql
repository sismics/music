create cached table T_ALBUM (
   ID                   varchar(36)          not null,
   DIRECTORY_ID         varchar(36)          not null,
   ARTIST_ID            varchar(36)          not null,
   NAME                 varchar(1000)        not null,
   ALBUMART             varchar(36)          null,
   LOCATION             varchar(2000)        not null,
   CREATEDATE           datetime             not null,
   UPDATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_ALBUM primary key (ID)
);

create unique index IDX_PK_ALBUM on T_ALBUM (
   ID
);

create  index IDX_FK_ALB_ARTIST_ID on T_ALBUM (
   ARTIST_ID
);

create  index IDX_FK_ALB_DIRECTORY_ID on T_ALBUM (
   DIRECTORY_ID
);

create cached table T_ARTIST (
   ID                   varchar(36)          not null,
   NAME                 varchar(1000)        not null,
   NAMECORRECTED        varchar(1000)        null,
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_ARTIST primary key (ID)
);

create unique index IDX_PK_ARTIST on T_ARTIST (
   ID
);

create memory table T_AUTHENTICATION_TOKEN (
   ID                   varchar(36)          not null,
   USER_ID              varchar(36)          not null,
   LONGLASTED           bit                  not null,
   CREATEDATE           datetime             not null,
   LASTCONNECTIONDATE   datetime             null,
   constraint PK_T_AUTHENTICATION_TOKEN primary key (ID)
);

create unique index IDX_PK_AUTHENTICATION_TOKEN on T_AUTHENTICATION_TOKEN (
   ID
);

create  index IDX_FK_AUT_USER_ID on T_AUTHENTICATION_TOKEN (
   USER_ID
);

create memory table T_CONFIG (
   ID                   varchar(50)          not null,
   VALUE                varchar(250)         not null,
   constraint PK_T_CONFIG primary key (ID)
);

create unique index IDX_PK_CONFIG on T_CONFIG (
   ID
);

create memory table T_DIRECTORY (
   ID                   varchar(36)          not null,
   LOCATION             varchar(1000)        null,
   CREATEDATE           datetime             not null,
   DISABLEDATE          datetime             null,
   DELETEDATE           datetime             null,
   constraint PK_T_DIRECTORY primary key (ID)
);

create unique index IDX_PK_DIRECTORY on T_DIRECTORY (
   ID
);

create memory table T_LOCALE (
   ID                   varchar(10)          not null,
   constraint PK_T_LOCALE primary key (ID)
);

create unique index IDX_PK_LOCALE on T_LOCALE (
   ID
);

create table T_PLAYER (
   ID                   VARCHAR(36)          not null,
   constraint PK_T_PLAYER primary key (ID)
);

create unique index IDX_PK_PLAYER on T_PLAYER (
   ID
);

create cached table T_PLAYLIST (
   ID                   varchar(36)          not null,
   USER_ID              varchar(36)          not null,
   NAME                 varchar(200)         null,
   constraint PK_T_PLAYLIST primary key (ID)
);

create unique index IDX_PK_PLAYLIST on T_PLAYLIST (
   ID
);

create cached table T_PLAYLIST_TRACK (
   ID                   varchar(36)          not null,
   PLAYLIST_ID          varchar(36)          not null,
   TRACK_ID             varchar(36)          not null,
   NUMBER               int                  not null,
   constraint PK_T_PLAYLIST_TRACK primary key (ID)
);

create unique index IDX_PK_PLAYLIST_TRACK on T_PLAYLIST_TRACK (
   ID
);

create  index IDX_FK_PLT_PLAYLIST_ID on T_PLAYLIST_TRACK (
   PLAYLIST_ID
);

create  index IDX_FK_PLT_TRACK_ID on T_PLAYLIST_TRACK (
   TRACK_ID
);

create memory table T_PRIVILEGE (
   ID                   varchar(20)          not null,
   constraint PK_T_PRIVILEGE primary key (ID)
);

create unique index IDX_PK_PRIVILEGE on T_PRIVILEGE (
   ID
);

create memory table T_ROLE (
   ID                   varchar(36)          not null,
   NAME                 varchar(50)          not null,
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_ROLE primary key (ID)
);

create unique index IDX_PK_ROLE on T_ROLE (
   ID
);

create memory table T_ROLE_PRIVILEGE (
   ID                   varchar(36)          not null,
   ROLE_ID              varchar(36)          not null,
   PRIVILEGE_ID         varchar(20)          not null,
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_ROLE_PRIVILEGE primary key (ID)
);

create unique index IDX_PK_ROLE_PRIVILEGE on T_ROLE_PRIVILEGE (
   ID
);

create  index IDX_RPR_PRIVILEGE_ID on T_ROLE_PRIVILEGE (
   PRIVILEGE_ID
);

create cached table T_TRACK (
   ID                   varchar(36)          not null,
   ALBUM_ID             varchar(36)          not null,
   ARTIST_ID            varchar(36)          not null,
   FILENAME             varchar(2000)        not null,
   TITLE                varchar(2000)        not null,
   TITLECORRECTED       varchar(2000)        null,
   YEAR                 integer              null,
   GENRE                varchar(100)         null,
   LENGTH               integer              not null,
   BITRATE              integer              not null,
   NUMBER               integer              null,
   VBR                  bit                  not null,
   FORMAT               varchar(10)          not null,
   PLAYCOUNT            int                  not null default '0',
   FAVORITE             bit                  not null default '0',
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_TRACK primary key (ID)
);

create unique index IDX_PK_TRACK on T_TRACK (
   ID
);

create  index IDX_PK_TRK_ARTIST_ID on T_TRACK (
   ARTIST_ID
);

create  index IDX_PK_TRK_ALBUM_ID on T_TRACK (
   ALBUM_ID
);

create memory table T_TRANSCODER (
   ID                   varchar(36)          not null,
   NAME                 varchar(100)         not null,
   SOURCE               varchar(1000)        not null,
   DESTINATION          varchar(100)         not null,
   STEP1                varchar(1000)        not null,
   STEP2                varchar(1000)        null,
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_TRANSCODER primary key (ID)
);

create unique index IDX_PK_TRANSCODER on T_TRANSCODER (
   ID
);

create memory table T_USER (
   ID                   varchar(36)          not null,
   LOCALE_ID            varchar(10)          not null,
   ROLE_ID              varchar(36)          not null,
   USERNAME             varchar(50)          not null,
   PASSWORD             varchar(60)          not null,
   EMAIL                varchar(100)         not null,
   MAXBITRATE           integer              null default '0',
   LASTFMSESSIONTOKEN   varchar(100)         null default '0',
   LASTFMACTIVE         bit                  not null default '0',
   FIRSTCONNECTION      bit                  not null default '0',
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_USER primary key (ID)
);

create unique index IDX_PK_USER on T_USER (
   ID
);

create  index IDX_FK_USE_LOCALE_ID on T_USER (
   LOCALE_ID
);

create  index IDX_FK_USE_ROLE_ID on T_USER (
   ROLE_ID
);

create cached table T_USER_ALBUM (
   ID                   varchar(36)          not null,
   USER_ID              varchar(36)          not null,
   ALBUM_ID             varchar(36)          not null,
   SCORE                int                  not null default '0',
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_USER_ALBUM primary key (ID)
);

create unique index IDX_PK_USER_ALBUM on T_USER_ALBUM (
   ID
);

create  index IDX_FK_USA_USER_ID on T_USER_ALBUM (
   USER_ID
);

create  index IDX_FK_USA_ALBUM_ID on T_USER_ALBUM (
   ALBUM_ID
);

create cached table T_USER_TRACK (
   ID                   varchar(36)          not null,
   USER_ID              varchar(36)          not null,
   TRACK_ID             varchar(36)          not null,
   PLAYCOUNT            int         not null default 0,
   LIKED                bit                  not null default '0',
   CREATEDATE           datetime             not null,
   DELETEDATE           datetime             null,
   constraint PK_T_USER_TRACK primary key (ID)
);

create unique index IDX_PK_USER_TRACK on T_USER_TRACK (
   ID
);

create  index IDX_FK_UST_USER_ID on T_USER_TRACK (
   USER_ID
);

create unique index IDX_FK_UST_TRACK_ID on T_USER_TRACK (
   TRACK_ID
);

alter table T_ALBUM
   add constraint fk_alb_artist_id foreign key (ARTIST_ID)
references T_ARTIST (ID)
on delete restrict on update restrict;

alter table T_ALBUM
   add constraint fk_alb_directory_id foreign key (DIRECTORY_ID)
references T_DIRECTORY (ID)
on delete restrict on update restrict;

alter table T_AUTHENTICATION_TOKEN
   add constraint fk_aut_user_id foreign key (USER_ID)
references T_USER (ID)
on delete restrict on update restrict;

alter table T_PLAYLIST
   add constraint fk_pll_user_id foreign key (USER_ID)
references T_USER (ID)
on delete restrict on update restrict;

alter table T_PLAYLIST_TRACK
   add constraint fk_plt_playlist_id foreign key (PLAYLIST_ID)
references T_PLAYLIST (ID)
on delete restrict on update restrict;

alter table T_PLAYLIST_TRACK
   add constraint fk_plt_track_id foreign key (TRACK_ID)
references T_TRACK (ID)
on delete restrict on update restrict;

alter table T_ROLE_PRIVILEGE
   add constraint fk_rpr_privilege_id foreign key (PRIVILEGE_ID)
references T_PRIVILEGE (ID)
on delete restrict on update restrict;

alter table T_ROLE_PRIVILEGE
   add constraint fk_rpr_role_id foreign key (ROLE_ID)
references T_ROLE (ID)
on delete restrict on update restrict;

alter table T_TRACK
   add constraint fk_trk_album_id foreign key (ALBUM_ID)
references T_ALBUM (ID)
on delete restrict on update restrict;

alter table T_TRACK
   add constraint fk_trk_artist_id foreign key (ARTIST_ID)
references T_ARTIST (ID)
on delete restrict on update restrict;

alter table T_USER
   add constraint fk_use_locale_id foreign key (LOCALE_ID)
references T_LOCALE (ID)
on delete restrict on update restrict;

alter table T_USER
   add constraint fk_use_role_id foreign key (ROLE_ID)
references T_ROLE (ID)
on delete restrict on update restrict;

alter table T_USER_ALBUM
   add constraint fk_usa_album_id foreign key (ALBUM_ID)
references T_ALBUM (ID)
on delete restrict on update restrict;

alter table T_USER_ALBUM
   add constraint fk_usa_user_id foreign key (USER_ID)
references T_USER (ID)
on delete restrict on update restrict;

alter table T_USER_TRACK
   add constraint fk_ust_track_id foreign key (TRACK_ID)
references T_TRACK (ID)
on delete restrict on update restrict;

alter table T_USER_TRACK
   add constraint fk_ust_user_id foreign key (USER_ID)
references T_USER (ID)
on delete restrict on update restrict;

create table T_USER_PLAY (
   ID                   VARCHAR(36)          not null,
   USER_ID              VARCHAR(36)          null,
   TRACK_ID             VARCHAR(36)          null,
   CREATEDATE           datetime             null,
   constraint PK_T_USER_PLAY primary key (ID)
);

create unique index IDX_PK_USER_PLAY on T_USER_PLAY (
   ID
);

create  index IDX_FK_USP_USER_ID on T_USER_PLAY (
   USER_ID
);

create  index IDX_FK_USP_TRACK_ID on T_USER_PLAY (
   TRACK_ID
);

alter table T_USER_PLAY
   add constraint fk_usp_track_id foreign key (TRACK_ID)
references T_TRACK (ID)
on delete restrict on update restrict;

alter table T_USER_PLAY
   add constraint fk_usp_user_id foreign key (USER_ID)
references T_USER (ID)
on delete restrict on update restrict;

insert into t_config(id,value) values('LAST_FM_API_KEY','7119a7b5c4455bbe8196934e22358a27');
insert into t_config(id,value) values('LAST_FM_API_SECRET','30dce5dfdb01b87af6038dd36f696f8a');
insert into t_config(id,value) values('DB_VERSION', '0');
insert into t_config(id,value) values('LUCENE_DIRECTORY_STORAGE', 'FILE');
insert into t_privilege(id) values('ADMIN');
insert into t_privilege(id) values('PASSWORD');
insert into t_privilege(id) values('IMPORT');
insert into t_locale(id) values('sq_AL');
insert into t_locale(id) values('sq');
insert into t_locale(id) values('ar_DZ');
insert into t_locale(id) values('ar_BH');
insert into t_locale(id) values('ar_EG');
insert into t_locale(id) values('ar_IQ');
insert into t_locale(id) values('ar_JO');
insert into t_locale(id) values('ar_KW');
insert into t_locale(id) values('ar_LB');
insert into t_locale(id) values('ar_LY');
insert into t_locale(id) values('ar_MA');
insert into t_locale(id) values('ar_OM');
insert into t_locale(id) values('ar_QA');
insert into t_locale(id) values('ar_SA');
insert into t_locale(id) values('ar_SD');
insert into t_locale(id) values('ar_SY');
insert into t_locale(id) values('ar_TN');
insert into t_locale(id) values('ar_AE');
insert into t_locale(id) values('ar_YE');
insert into t_locale(id) values('ar');
insert into t_locale(id) values('be_BY');
insert into t_locale(id) values('be');
insert into t_locale(id) values('bg_BG');
insert into t_locale(id) values('bg');
insert into t_locale(id) values('ca_ES');
insert into t_locale(id) values('ca');
insert into t_locale(id) values('zh_CN');
insert into t_locale(id) values('zh_HK');
insert into t_locale(id) values('zh_SG');
insert into t_locale(id) values('zh_TW');
insert into t_locale(id) values('zh');
insert into t_locale(id) values('hr_HR');
insert into t_locale(id) values('hr');
insert into t_locale(id) values('cs_CZ');
insert into t_locale(id) values('cs');
insert into t_locale(id) values('da_DK');
insert into t_locale(id) values('da');
insert into t_locale(id) values('nl_BE');
insert into t_locale(id) values('nl_NL');
insert into t_locale(id) values('nl');
insert into t_locale(id) values('en_AU');
insert into t_locale(id) values('en_CA');
insert into t_locale(id) values('en_IN');
insert into t_locale(id) values('en_IE');
insert into t_locale(id) values('en_MT');
insert into t_locale(id) values('en_NZ');
insert into t_locale(id) values('en_PH');
insert into t_locale(id) values('en_SG');
insert into t_locale(id) values('en_ZA');
insert into t_locale(id) values('en_GB');
insert into t_locale(id) values('en_US');
insert into t_locale(id) values('en');
insert into t_locale(id) values('et_EE');
insert into t_locale(id) values('et');
insert into t_locale(id) values('fi_FI');
insert into t_locale(id) values('fi');
insert into t_locale(id) values('fr_BE');
insert into t_locale(id) values('fr_CA');
insert into t_locale(id) values('fr_FR');
insert into t_locale(id) values('fr_LU');
insert into t_locale(id) values('fr_CH');
insert into t_locale(id) values('fr');
insert into t_locale(id) values('de_AT');
insert into t_locale(id) values('de_DE');
insert into t_locale(id) values('de_LU');
insert into t_locale(id) values('de_CH');
insert into t_locale(id) values('de');
insert into t_locale(id) values('el_CY');
insert into t_locale(id) values('el_GR');
insert into t_locale(id) values('el');
insert into t_locale(id) values('iw_IL');
insert into t_locale(id) values('iw');
insert into t_locale(id) values('hi_IN');
insert into t_locale(id) values('hu_HU');
insert into t_locale(id) values('hu');
insert into t_locale(id) values('is_IS');
insert into t_locale(id) values('is');
insert into t_locale(id) values('in_ID');
insert into t_locale(id) values('in');
insert into t_locale(id) values('ga_IE');
insert into t_locale(id) values('ga');
insert into t_locale(id) values('it_IT');
insert into t_locale(id) values('it_CH');
insert into t_locale(id) values('it');
insert into t_locale(id) values('ja_JP');
insert into t_locale(id) values('ja_JP_JP');
insert into t_locale(id) values('ja');
insert into t_locale(id) values('ko_KR');
insert into t_locale(id) values('ko');
insert into t_locale(id) values('lv_LV');
insert into t_locale(id) values('lv');
insert into t_locale(id) values('lt_LT');
insert into t_locale(id) values('lt');
insert into t_locale(id) values('mk_MK');
insert into t_locale(id) values('mk');
insert into t_locale(id) values('ms_MY');
insert into t_locale(id) values('ms');
insert into t_locale(id) values('mt_MT');
insert into t_locale(id) values('mt');
insert into t_locale(id) values('no_NO');
insert into t_locale(id) values('no_NO_NY');
insert into t_locale(id) values('no');
insert into t_locale(id) values('pl_PL');
insert into t_locale(id) values('pl');
insert into t_locale(id) values('pt_BR');
insert into t_locale(id) values('pt_PT');
insert into t_locale(id) values('pt');
insert into t_locale(id) values('ro_RO');
insert into t_locale(id) values('ro');
insert into t_locale(id) values('ru_RU');
insert into t_locale(id) values('ru');
insert into t_locale(id) values('sr_BA');
insert into t_locale(id) values('sr_ME');
insert into t_locale(id) values('sr_CS');
insert into t_locale(id) values('sr_RS');
insert into t_locale(id) values('sr');
insert into t_locale(id) values('sk_SK');
insert into t_locale(id) values('sk');
insert into t_locale(id) values('sl_SI');
insert into t_locale(id) values('sl');
insert into t_locale(id) values('es_AR');
insert into t_locale(id) values('es_BO');
insert into t_locale(id) values('es_CL');
insert into t_locale(id) values('es_CO');
insert into t_locale(id) values('es_CR');
insert into t_locale(id) values('es_DO');
insert into t_locale(id) values('es_EC');
insert into t_locale(id) values('es_SV');
insert into t_locale(id) values('es_GT');
insert into t_locale(id) values('es_HN');
insert into t_locale(id) values('es_MX');
insert into t_locale(id) values('es_NI');
insert into t_locale(id) values('es_PA');
insert into t_locale(id) values('es_PY');
insert into t_locale(id) values('es_PE');
insert into t_locale(id) values('es_PR');
insert into t_locale(id) values('es_ES');
insert into t_locale(id) values('es_US');
insert into t_locale(id) values('es_UY');
insert into t_locale(id) values('es_VE');
insert into t_locale(id) values('es');
insert into t_locale(id) values('sv_SE');
insert into t_locale(id) values('sv');
insert into t_locale(id) values('th_TH');
insert into t_locale(id) values('th_TH_TH');
insert into t_locale(id) values('th');
insert into t_locale(id) values('tr_TR');
insert into t_locale(id) values('tr');
insert into t_locale(id) values('uk_UA');
insert into t_locale(id) values('uk');
insert into t_locale(id) values('vi_VN');
insert into t_locale(id) values('vi');
insert into t_role(id,name,createdate) values('admin', 'Admin', NOW());
insert into t_role_privilege(id,role_id,privilege_id,createdate) values('admin_ADMIN', 'admin', 'ADMIN', NOW());
insert into t_role_privilege(id,role_id,privilege_id,createdate) values('admin_PASSWORD', 'admin', 'PASSWORD', NOW());
insert into t_role_privilege(id,role_id,privilege_id,createdate) values('admin_IMPORT', 'admin', 'IMPORT', NOW());
insert into t_role(id,name,createdate) values('user', 'User', NOW());
insert into t_role_privilege(id,role_id,privilege_id,createdate) values('user_PASSWORD', 'user', 'PASSWORD', NOW());
insert into t_role_privilege(id,role_id,privilege_id,createdate) values('user_IMPORT', 'user', 'IMPORT', NOW());
insert into t_user(id,locale_id,role_id,username,password,email,firstconnection,createdate) values('admin', 'en', 'admin', 'admin', '$2a$05$6Ny3TjrW3aVAL1or2SlcR.fhuDgPKp5jp.P9fBXwVNePgeLqb4i3C', 'admin@localhost', true, NOW());
insert into t_playlist(id,user_id) values('admin', 'admin');
