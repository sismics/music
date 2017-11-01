alter table T_USER alter column LASTFMSESSIONTOKEN set default null;
update T_USER set LASTFMSESSIONTOKEN = null where LASTFMSESSIONTOKEN = '0';