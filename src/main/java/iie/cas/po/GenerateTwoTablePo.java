package iie.cas.po;

import java.io.Serializable;
import java.util.HashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName="generatetwo")
public class GenerateTwoTablePo extends HashMap<String, String> implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@Field(type = FieldType.Keyword)
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
