package iie.cas.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

import iie.cas.po.GenerateTablePo;
import iie.cas.po.GenerateTwoTablePo;
@Component
public interface GenerateTwoTableElasticsearchRepository extends ElasticsearchRepository<GenerateTwoTablePo, String>{

}
