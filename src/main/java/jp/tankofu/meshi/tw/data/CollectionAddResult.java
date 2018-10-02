package jp.tankofu.meshi.tw.data;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionAddResult {
	public static class Response {
		private List<Map<String, Object>> errors;

		@JsonProperty("errors")
		public void setErrors(List<Map<String, Object>> errors) {
			this.errors = errors;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
	}

	@SuppressWarnings("unused")
	private Object objects;
	private Response response;

	@JsonProperty("objects")
	public void setObjects(Object objects) {
		this.objects = objects;
	}

	@JsonProperty("response")
	public void setResponse(Response response) {
		this.response = response;
	}

	@JsonIgnore
	public List<Map<String, Object>> getErrors() {
		// FIXME return deep copy.
		return response.errors;
	}

	@JsonIgnore
	public boolean isSuccess() {
		return response.errors.isEmpty();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
