package iie.cas.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

import iie.cas.po.GenerateTablePo;
@Component
public interface GenerateTableElasticsearchRepository extends ElasticsearchRepository<GenerateTablePo, String>{

}
