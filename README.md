yabe
====
SNS关系实现

 SNS关系分两种：follow/fans
 
 前期通过通讯录，创建人与人间的link关系，通讯录中的人是本人的好友，即本人关注了这些人，同时本人是这些人的fans。
 
 发布商品，只能对fans或二度fans可见
 
 mysql结合Redis实现
 
## Redis存储结构

* 通讯录fans关系
```
通讯录中电话 -> 用户电话
如：
t_1360003 -> {135000,136000,136001}
```

* 用户fans关系
```
用户ID->粉丝ID
如:
f_100 -> {101}
```
* 用户follow关系
```
用户ID->关注人ID
如:
g_101 -> {100}
```
* 电话到Uers的映射
```
用户电话->用户ID
如:
a_1360003 -> 100
```
* 商品发布列表
```
用户ID->商品ID集合(goodsid,pubtime)
如:
p_100 -> [1^130000,] 
```
* 商品推送列表
```
用户ID->推送商品ID集合(goodsid,linktype,sellerid)
如:
c_100 -> [1^1^2,]
```
* 联系人nick
```
用户ID+联系人ID->nick
如:
n_100_101 -> xx
```

## spring引入
```
<bean id="redis" class="redis.clients.jedis.Jedis">
	<constructor-arg>
		<value>112.124.40.161</value>
	</constructor-arg>
</bean>
<bean id="snsService" class="com.yabe.core.service.SNSService">
	<property name="redis" ref="redis"></property>
</bean>
```
