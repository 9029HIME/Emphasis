# 问题背景

假设我有这么一张用户登录表t_user_login_record，有一个【用户登录界面】需要展示用户登录信息，界面需要分页展示。

一般来说，第一页的数据会使用这样的sql进行查询↓

```sql
SELECT * FROM t_user_login_record ORDER BY login_time DESC LIMIT 0,10;
```

随着项目的持续运行，用户登录表数据突破50多万，当我要分页查询第5000页的数据，SQL就是这样的↓

```sql
SELECT * FROM t_user_login_record ORDER BY login_time DESC LIMIT 50000,10
```

需要注意的是，这样的语句只会走全表扫描，先扫到第50000行数据，再往后面截取10个，显然效率会很底下，经过实际测试能达到 n x 10秒级别。

**那么问题来了，对这种大分页的查询，能有什么好的优化手段呢？**

# 方案1-覆盖索引

即使我们给login_time建立索引，这样的语句也是不会命中的↓

```sql
SELECT * FROM t_user_login_record ORDER BY login_time DESC LIMIT 50000,10
```

因为只使用ORDER，不使用WHERE并且SELECT *，是不会命中索引的，或许可以尝试【先通过覆盖索引拿到起始login_time】，再通过WHERE来命中login_time的条件：

```sql
SELECT * FROM t_user_login_record WHERE login_time <= 
	(SELECT login_time FROM t_user_login_record ORDER BY login_time DESC LIMIT 50000,1)
ORDER BY login_time DESC LIMIT 10;
```

# 方案2-连续翻页设计

在连续翻页的前提下（只能+1-1），前端可以采用将【当前页】最后一条记录的login_time作为条件，传递给后端，后端将这个条件作为参数进行查询

比如当前页的最后一条记录，它的login_time是 2022-10-21 10:00:01，那么点击下一页，实际查询sql是

```sql
SELECT * FROM t_user_login_record WHERE login_time < '2022-10-21 10:00:01' ORDER BY login_time DESC LIMIT 50000,10
```

**但其实这样的查询是有问题的，如果login_time相等的数据有多个，那么会出现漏查的情况，在这种情况下，我们可以考虑【根据id倒叙】。**

# 方案3-Elasticsearch

不管是方案1还是方案2，都只适用于【无查询条件或查询条件与排序条件相等】的大分页，实际的查询页面肯定要满足多条件的查询，在多条件场景下就不适合了。因此可以考虑上Elasticsearch，通过Es的倒排索引和数据汇总机制，来完成带条件的大分页查询。

# 思考

要知道，Es的分页是逻辑分页，它是将所有数据节点的查询结果汇总到协调节点上，协调节点将这笔大数据集排好序，再进行截取分页。在分页过程上的短板和MySQL是类似的。

只不过Es作为天然的分布式搜索引擎，稍微弥补了一下短板，但超大分页的情况下会压垮协调节点。**因此Es默认一次查询最多查10000条数据，它也不是解决大分页的“银弹”，**

MySQL不适合，Es也不太适合，难道就没有一个完美的解决办法嘛？其实我陷入了一个误区，在一个页面查询上，用户真的需要知道5000页后面的数据吗？在99%的场景下其实是不需要关心的，基本上前100页的数据已经能够满足使用了。如果数据的滞后性很强，也可以通过 条件限定 来获取结果，而不是手动的跳到最后一页查看。哪怕是淘宝京东这种体量的项目，大部分的查询功能要么仅支持无条件连续翻页，要么仅允许用户翻有限数量的页面。

因此没必要过于纠结大分页的处理方案，很多时候在业务设计就可以解决了：直接不允许。