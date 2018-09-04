package com.open.es;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.open.exception.OnRuntimeException;
import com.open.manage.IResourceWithId;

public abstract class EsService<K, T extends IResourceWithId<K>> {

	private static final Logger logger = LoggerFactory
			.getLogger(EsService.class);
	private EsTransportClient esTransportClient;
	private Integer retryOnConflict = 5;

	protected UpdateRequestBuilder buildUpdateRequest(String indexName,
			String indexType, Object id, XContentBuilder source) {
		UpdateRequestBuilder builder = esTransportClient.getClient()
				.prepareUpdate(indexName, indexType, String.valueOf(id));
		builder = builder.setDoc(source).setRetryOnConflict(retryOnConflict)
				.setDocAsUpsert(true);
		return builder;
	}

	protected RefreshRequest buildRefreshRequest(String indexName) {
		return new RefreshRequest(indexName);
	}

	protected DeleteIndexRequest buildDeleteIndexRequest(String indexName) {
		return new DeleteIndexRequest(indexName);
	}

	protected boolean isExistIndex(String indexName) {
		try {
			Client client = getEsTransportClient().getClient();
			return client.admin().indices().prepareExists(indexName).execute()
					.actionGet().isExists();
		} catch (Exception e) {
			throw new OnRuntimeException("failed in judge exist index:"
					+ indexName, e);
		}
	}

	/**
	 * 创建索引，强制删除重复索引
	 * 
	 * @param indexName
	 * @param indexType
	 * @param mapping
	 * @return
	 */
	protected void createIndexForce(String indexName, String indexType,
			XContentBuilder mapping) {
		try {
			// delete if exist
			deleteIndex(indexName);
			Client client = getEsTransportClient().getClient();
			// create index
			client.admin().indices().prepareCreate(indexName).execute()
					.actionGet();
			PutMappingRequest request = Requests.putMappingRequest(indexName)
					.type(indexType).source(mapping);
			PutMappingResponse response = client.admin().indices()
					.putMapping(request).actionGet();
			logger.info("create index {}:{}", indexName,
					response.isAcknowledged());
		} catch (Exception e) {
			throw new OnRuntimeException("failed in create index:" + indexName,
					e);
		}
	}

	/**
	 * 创建索引，不创建重复索引
	 * 
	 * @param indexName
	 * @param indexType
	 * @param mapping
	 * @return
	 */

	protected boolean createIndex(String indexName, String indexType,
			XContentBuilder mapping) {
		try {
			// delete if exist
			boolean indexExistence = isExistIndex(indexName);
			if (indexExistence) {
				return false;
			}
			Client client = getEsTransportClient().getClient();
			// create index
			client.admin().indices().prepareCreate(indexName).execute()
					.actionGet();
			PutMappingRequest request = Requests.putMappingRequest(indexName)
					.type(indexType).source(mapping);
			PutMappingResponse response = client.admin().indices()
					.putMapping(request).actionGet();
			logger.info("create index {}:{}", indexName,
					response.isAcknowledged());

			return true;
		} catch (Exception ex) {
			logger.error("failed in creating index:" + indexName, ex);
			return false;
		}
	}

	/**
	 * 删除索引
	 * 
	 * @param indexName
	 * @return
	 */
	protected void deleteIndex(String indexName) {
		try {
			IndicesAdminClient client = getEsTransportClient().getClient()
					.admin().indices();
			boolean exist = client.prepareExists(indexName).execute()
					.actionGet().isExists();
			if (!exist) {
				logger.info("index:{} not exist!", indexName);
				return;
			}
			DeleteIndexResponse deleteResponse = client.delete(
					new DeleteIndexRequest(indexName)).actionGet();
			logger.info("delete index:{}, response:{}", indexName,
					deleteResponse.isAcknowledged());
			exist = client.prepareExists(indexName).execute().actionGet()
					.isExists();
			if (!exist) {
				logger.info("index:{} deleted!", indexName);
			} else {
				throw new OnRuntimeException("failed in delete index:"
						+ indexName);
			}
		} catch (Exception e) {
			throw new OnRuntimeException("failed in delete index:" + indexName,
					e);
		}
	}

	public EsTransportClient getEsTransportClient() {
		return esTransportClient;
	}

	public void setEsTransportClient(EsTransportClient esTransportClient) {
		this.esTransportClient = esTransportClient;
	}

	public Integer getRetryOnConflict() {
		return retryOnConflict;
	}

	public void setRetryOnConflict(Integer retryOnConflict) {
		this.retryOnConflict = retryOnConflict;
	}

	/**
	 * 批量添加数据到索引
	 * 
	 * @param indexName
	 * @param indexType
	 * @param object
	 * @return
	 */
	public void addDataList(String indexName, String indexType, List<T> dataList) {
		Client client = getEsTransportClient().getClient();
		if (dataList != null) {
			int size = dataList.size();
			if (size > 0) {
				BulkRequestBuilder bulkRequest = client.prepareBulk();
				for (int i = 0; i < size; ++i) {
					T data = dataList.get(i);
					String jsonSource = getIndexDataFromObj(data);
					if (jsonSource != null) {
						bulkRequest.add(client
								.prepareIndex(indexName, indexType,
										data.getResourceId().toString())
								.setRefresh(true).setSource(jsonSource));
					}
				}

				BulkResponse bulkResponse = bulkRequest.execute().actionGet();
				if (bulkResponse.hasFailures()) {
					Iterator<BulkItemResponse> iter = bulkResponse.iterator();
					while (iter.hasNext()) {
						BulkItemResponse itemResponse = iter.next();
						if (itemResponse.isFailed()) {
							logger.error(itemResponse.getFailureMessage());

						}
					}
				}
			}
		}
	}

	/**
	 * 添加数据到索引
	 * 
	 * @param indexName
	 * @param indexType
	 * @param object
	 * @return
	 */

	public boolean addData(String indexName, String indexType, T object) {
		Client client = getEsTransportClient().getClient();
		String jsonSource = getIndexDataFromObj(object);
		if (jsonSource != null) {
			IndexRequestBuilder requestBuilder = client.prepareIndex(indexName,
					indexType).setRefresh(true);
			requestBuilder.setSource(jsonSource).execute().actionGet();
			return true;
		}

		return false;
	}

	/**
	 * 更新数据到索引
	 * 
	 * @param indexName
	 * @param indexType
	 * @param object
	 * @return
	 */

	public boolean updateData(String indexName, String indexType, T object) {
		Client client = getEsTransportClient().getClient();
		String jsonSource = getIndexDataFromObj(object);
		if (jsonSource != null) {
			UpdateRequestBuilder requestBuilder = client.prepareUpdate(
					indexName, indexType, object.getResourceId().toString()).setRefresh(
					true);
			requestBuilder.setDoc(jsonSource).execute().actionGet();
			
			return true;
		}

		return false;
	}

	/**
	 * 执行搜索
	 * 
	 * @param queryBuilder
	 * @param indexname
	 * @param type
	 * @return
	 */
	public List<T> searcher(QueryBuilder queryBuilder, String indexname,
			String type) {
		Client client = getEsTransportClient().getClient();
		List<T> list = new ArrayList<T>();
		SearchResponse searchResponse = client.prepareSearch(indexname)
				.setTypes(type).setQuery(queryBuilder).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		System.out.println("查询到记录数=" + hits.getTotalHits());
		SearchHit[] searchHists = hits.getHits();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {

				list.add(getObjFormHit(hit));

			}
		}
		return list;
	}

	public abstract T getObjFormHit(SearchHit hit);

	public abstract String getIndexDataFromObj(T obj);

}
