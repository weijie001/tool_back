replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_shirt','初始球衣[id]','{"ALL":0}','[2001,2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','use_home_shirt','默认主场使用球衣[id]','{"RAND":1}','[2001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','use_away_shirt','默认客场场使用球衣[id]','{"RAND":1}','[2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_stadium','初始球场','{"ALL":0}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','use_stadium','默认使用球场','{"RAND":1}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_tifo_image','初始tifo图案','{"ALL":0}','[3102]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_tifo_texture','初始tifo纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_brand_image','初始举牌图案','{"ALL":0}','[3301]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('1','init_brand_texture','初始举牌纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_shirt','初始球衣[id]','{"ALL":0}','[2001,2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','use_home_shirt','默认主场使用球衣[id]','{"RAND":1}','[2001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','use_away_shirt','默认客场场使用球衣[id]','{"RAND":1}','[2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_stadium','初始球场','{"ALL":0}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','use_stadium','默认使用球场','{"RAND":1}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_tifo_image','初始tifo图案','{"ALL":0}','[3101]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_tifo_texture','初始tifo纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_brand_image','初始举牌图案','{"ALL":0}','[3301]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('2','init_brand_texture','初始举牌纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_shirt','初始球衣[id]','{"ALL":0}','[2001,2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','use_home_shirt','默认主场使用球衣[id]','{"RAND":1}','[2001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','use_away_shirt','默认客场场使用球衣[id]','{"RAND":1}','[2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_stadium','初始球场','{"ALL":0}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','use_stadium','默认使用球场','{"RAND":1}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_tifo_image','初始tifo图案','{"ALL":0}','[3101]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_tifo_texture','初始tifo纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_brand_image','初始举牌图案','{"ALL":0}','[3301]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('3','init_brand_texture','初始举牌纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','player','初始球员[球员id, ...]','{"ALL":0}','[1273,1383,1989,2437,1566,2079,2148,2679,3012,2595,2783,1253,2122,2539,2400,2545,2896,3016]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','duty','[队长id,任意球,点球,左,右角球]','{"ALL":1}','[1383,1383,1383,1383,1253]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','offensive','核心球员[id, ...]','{"RAND":1}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','starter','首发球员[id, ...]配合阵型顺序','{"ALL":0}','[1273,1383,1989,2437,1566,2079,2148,2679,3012,2595,2783]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','bench','替补[id, ...]','{"ALL":0}','[1253,2122,2539,2400,2545,2896,3016]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_squad','初始阵型[id, ...]','{"ALL":0}','[1014]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_squad','使用阵型[id, ...]配合首发顺序','{"RAND":1}','[1014]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_tactic','初始战术[id, ...]','{"ALL":0}','[301,302,303,304,305,306,307,308]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_atk_tactic','默认攻击战术[id]','{"RAND":1}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_def_tactic','默认防守战术[id]','{"RAND":1}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','tactic_name','默认战术名前缀','{"STR":0}','TACTIC');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_shirt','初始球衣[id]','{"ALL":0}','[2001,2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_home_shirt','默认主场使用球衣[id]','{"RAND":1}','[2001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_away_shirt','默认客场场使用球衣[id]','{"RAND":1}','[2002]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_stadium','初始球场','{"ALL":0}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_stadium','默认使用球场','{"RAND":1}','[3001]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_tifo_image','初始tifo图案','{"ALL":0}','[3101]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_tifo_texture','初始tifo纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_brand_image','初始举牌图案','{"ALL":0}','[3301]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','init_brand_texture','初始举牌纹理','{"ALL":0}','[]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','use_badge','默认队徽','{"RAND":1}','[10000]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','gala_coin','gala币','{"ALL":0}','[0]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','game_coin','游戏币欧元','{"ALL":0}','[0]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','scout_coin','球探币','{"ALL":0}','[0]');
replace into t_init_team_rule(`way`,`key`,`desc`,`choice`,`content`) values('4','item_map','初始化道具','{"MAP":0}','[]');
