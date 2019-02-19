package simples;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import examples.istat.cp2011.CP2011;
import examples.istat.cp2011.CP2011Item;
import simples.utilities.JSON;

public class MainESLocalJava {

	public static void main(String[] args) throws Exception {

		String _index = "cp2011java";
		String _type = "doc";

		ESLocal es = ESHelper.local("src/main/resources/conf/es-local.conf");
		es.start();

		es.index_delete(_index);
		es.index_create(_index, _type, "src/main/resources/data/ISTAT/_settings.json",
				"src/main/resources/data/ISTAT/_mapping.json");

		List<CP2011Item> elements = CP2011.toJavaList();

		for (int i = 0; i < elements.size(); i++) {

			try {

				CP2011Item item = elements.get(i);
				String _id = String.format("%04d", Integer.valueOf(i));
				String _source = JSON.writer().writeValueAsString(item);

				es.indexing(_index, _type, _id, _source);

			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		Thread.sleep(120000);
		es.stop();

	}

}
