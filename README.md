# tk-mybatis-generator
扩展：
1 支持mysql,postgresql,mariadb
2 自定义包名(service层，model层,controller层，mapper层)

使用说明：
1 获取源码
2 修改application.yml配置文件为自己对应的配置，主要修改项也许包含以下项：
    2.1 数据库连接信息(数据库驱动，连接地址，用户名，用户密码)
    2.2 忽略生成的模板
    vm-template:
      ignore:
    2.3 包名，package:下各配置项
    2.4 commonBusinessBiz，commonController，commonMapper三项所对应的基础类应率先在自己项目中创建好，示例可参见：
    
    commonBusinessBiz：
    public class BusinessBiz<M extends CommonMapper<T>, T> extends BaseBiz<M,T>{
    
        @Override
        public int insertSelectiveBindCrt(T entity) {
            return super.insertSelective(entity);
        }
    
        @Override
        public int updateSelectiveByIdBindUpd(T entity) {
            return super.updateSelectiveById(entity);
        }
    }
    
    BaseBiz：
    public abstract class BaseBiz<M extends CommonMapper<T>, T> {
    	@Autowired
    	protected M mapper;
    
    	public void setMapper(M mapper) {
    		this.mapper = mapper;
    	}
    
    	public T selectOne(T entity) {
    		return mapper.selectOne(entity);
    	}
    
    	public T selectById(Object id) {
    		return mapper.selectByPrimaryKey(id);
    	}
    
    	/**
    	 * 选择性按ID查询
    	 * 
    	 * @param id
    	 *            实例ID
    	 * @param clazz
    	 * @param arg0
    	 *            待查询的字段
    	 * @return
    	 */
    	public T selectPropertiesById(Object id, Class<T> clazz, String... arg0) {
    		Example example = new Example(clazz).selectProperties(arg0);
    		example.createCriteria().andEqualTo("id", id);
    		List<T> instanceList = this.selectByExample(example);
    		if (EmptyUtil.isNotEmpty(instanceList)) {
    			return instanceList.get(0);
    		}
    		return null;
    	}
    
    	/**
    	 * 选择性按ID查询<br/>
    	 * 该方法可指定记录ID的字段名称
    	 * 
    	 * @param id
    	 *            实例ID
    	 * @param idField
    	 *            ID对应的字段名称，如果ID字段就是"id",可直接使用
    	 *            com.hgd.provider.commom.biz.BaseBiz.selectProperties(Object,
    	 *            Class<T>, String...)
    	 * @param clazz
    	 * @param arg0
    	 *            待查询的字段
    	 * @return
    	 */
    	public T selectPropertiesById(Object id, String idField, Class<T> clazz, String... arg0) {
    		Example example = new Example(clazz).selectProperties(arg0);
    		example.createCriteria().andEqualTo(idField, id);
    		List<T> instanceList = this.selectByExample(example);
    		if (EmptyUtil.isNotEmpty(instanceList)) {
    			return instanceList.get(0);
    		}
    		return null;
    	}
    
    	/**
    	 * 查询排除arg0后的字段
    	 * 
    	 * @param id
    	 *            实例ID
    	 * @param clazz
    	 * @param arg0
    	 *            待排除的字段
    	 * @return
    	 */
    	public T excludePropertiesById(Object id, Class<T> clazz, String... arg0) {
    		Example example = new Example(clazz).excludeProperties(arg0);
    		example.createCriteria().andEqualTo("id", id);
    		List<T> instanceList = this.selectByExample(example);
    		if (EmptyUtil.isNotEmpty(instanceList)) {
    			return instanceList.get(0);
    		}
    		return null;
    	}
    
    	/**
    	 * 查询排除arg0后的字段<br/>
    	 * 该方法可指定记录ID的字段名称
    	 * 
    	 * @param id
    	 *            实例ID
    	 * @param idField ID对应的字段名称，如果ID字段就是"id",可直接使用
    	 * com.hgd.provider.commom.biz.BaseBiz.excludeProperties(Object, Class<T>, String...)
    	 * @param clazz
    	 * @param arg0
    	 *            待排除的字段
    	 * @return
    	 */
    	public T excludePropertiesById(Object id, String idField, Class<T> clazz, String... arg0) {
    		Example example = new Example(clazz).excludeProperties(arg0);
    		example.createCriteria().andEqualTo(idField, id);
    		List<T> instanceList = this.selectByExample(example);
    		if (EmptyUtil.isNotEmpty(instanceList)) {
    			return instanceList.get(0);
    		}
    		return null;
    	}
    
    	public List<T> selectList(T entity) {
    		return mapper.select(entity);
    	}
    
    	public List<T> selectListAll() {
    		return mapper.selectAll();
    	}
    
    	public Long selectCount(T entity) {
    		return new Long(mapper.selectCount(entity));
    	}
    
    	public int insertSelective(T entity) {
    		return mapper.insertSelective(entity);
    	}
    
    	public int delete(T entity) {
    		return mapper.delete(entity);
    	}
    
    	public int deleteById(Object id) {
    		return mapper.deleteByPrimaryKey(id);
    	}
    
    	public int updateById(T entity) {
    		return mapper.updateByPrimaryKey(entity);
    	}
    
    	public int updateSelectiveById(T entity) {
    		return mapper.updateByPrimaryKeySelective(entity);
    	}
    
    	public List<T> selectByExample(Object example) {
    		return mapper.selectByExample(example);
    	}
    
    	public int selectCountByExample(Object example) {
    		return mapper.selectCountByExample(example);
    	}
    
    	public TableResultResponse<T> selectByQuery(Query query) {
    		@SuppressWarnings("unchecked")
    		Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    		Example example = new Example(clazz);
    		query2criteria(query, example);
    		Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
    		List<T> list = this.selectByExample(example);
    		return new TableResultResponse<T>(result.getTotal(), list);
    	}
    
    	public void query2criteria(Query query, Example example) {
    		if (query.entrySet().size() > 0) {
    			Example.Criteria criteria = example.createCriteria();
    			for (Map.Entry<String, Object> entry : query.entrySet()) {
    				criteria.andEqualTo(entry.getKey(), "%" + entry.getValue().toString() + "%");
    			}
    		}
    	}
    
    	/**
    	 * 插入记录
    	 * 该方法会检测entity中是否含有crtTime,crtUserId,crtUserName,crtTenantId属性
    	 * 如果有，且属性为空，则向属性中注入默认值
    	 * @param entity 待插入的实体
    	 */
    	public abstract int insertSelectiveBindCrt(T entity);
    
    	/**
    	 * 更新记录
    	 * 该方法会检测entity中是否含有updTime,updUserId,updUserName属性
    	 * 如果有，且属性为空，则向属性中注入默认值
    	 * @param entity 待更新的实体
    	 */
    	public abstract int updateSelectiveByIdBindUpd(T entity);
    }
    
    commonController:
    public class BaseController<Biz extends BaseBiz,Entity,PK> {
        @Autowired
        protected  Biz baseBiz;
    }
    
    commonMapper:
    public interface CommonMapper<T> extends SelectByIdsMapper<T>,Mapper<T> {
    }
    
    说明：
    BusinessBiz内对BaseBiz中抽象方法的两个实现逻辑我给删除的，因为这个是针对我自己的项目实现的，大家可以根据自己的项目要求进行实现，不进行实现也不关紧要。
    有些类里面是空白，这并不奇怪
    
3 修改好配置后，启动项目
4 在浏览器访问地址：http://localhost:4444/index
5 代码生成后是一个.zip压缩包，将其解压后直接复制到自己的项目中即可