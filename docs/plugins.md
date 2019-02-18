
org.elasticsearch.transport.Netty4Plugin
org.elasticsearch.xpack.indexlifecycle.IndexLifecycle
org.elasticsearch.analysis.common.CommonAnalysisPlugin
org.elasticsearch.index.reindex.ReindexPlugin
org.elasticsearch.percolator.PercolatorPlugin
org.elasticsearch.xpack.graph.Graph
org.elasticsearch.ingest.common.IngestCommonPlugin
org.elasticsearch.xpack.rollup.Rollup
org.elasticsearch.painless.PainlessPlugin
org.elasticsearch.xpack.core.XPackPlugin
org.elasticsearch.xpack.ccr.Ccr
org.elasticsearch.plugin.repository.url.URLRepositoryPlugin
org.elasticsearch.xpack.monitoring.Monitoring
org.elasticsearch.search.aggregations.matrix.MatrixAggregationPlugin
org.elasticsearch.xpack.watcher.Watcher
org.elasticsearch.tribe.TribePlugin
org.elasticsearch.xpack.deprecation.Deprecation
org.elasticsearch.xpack.logstash.Logstash
org.elasticsearch.xpack.upgrade.Upgrade
org.elasticsearch.index.mapper.MapperExtrasPlugin
org.elasticsearch.xpack.sql.plugin.SqlPlugin
org.elasticsearch.script.mustache.MustachePlugin
org.elasticsearch.xpack.ml.MachineLearning
org.elasticsearch.script.expression.ExpressionPlugin
org.elasticsearch.index.rankeval.RankEvalPlugin
org.elasticsearch.xpack.security.Security
org.elasticsearch.join.ParentJoinPlugin


----




   //      classOf[org.elasticsearch.transport.Netty4Plugin],
    //classOf[org.elasticsearch.analysis.common.CommonAnalysisPlugin],
    //      classOf[org.elasticsearch.index.reindex.ReindexPlugin],
    //      classOf[org.elasticsearch.percolator.PercolatorPlugin],
    //classOf[org.elasticsearch.ingest.common.IngestCommonPlugin],
    //classOf[org.elasticsearch.painless.PainlessPlugin],
    //classOf[org.elasticsearch.plugin.repository.url.URLRepositoryPlugin],
    //classOf[org.elasticsearch.search.aggregations.matrix.MatrixAggregationPlugin],
    //classOf[org.elasticsearch.tribe.TribePlugin],
    //classOf[org.elasticsearch.script.mustache.MustachePlugin],
    //classOf[org.elasticsearch.script.expression.ExpressionPlugin],
    //classOf[org.elasticsearch.index.rankeval.RankEvalPlugin],
    //classOf[org.elasticsearch.index.mapper.MapperExtrasPlugin],
    //classOf[org.elasticsearch.xpack.graph.Graph],
    //classOf[org.elasticsearch.xpack.rollup.Rollup],
    //classOf[org.elasticsearch.xpack.monitoring.Monitoring],
    //classOf[org.elasticsearch.xpack.watcher.Watcher],
    //classOf[org.elasticsearch.xpack.deprecation.Deprecation],
    //classOf[org.elasticsearch.xpack.core.XPackPlugin],
    //classOf[org.elasticsearch.xpack.ccr.Ccr],
    //classOf[org.elasticsearch.xpack.logstash.Logstash],
    //classOf[org.elasticsearch.xpack.upgrade.Upgrade],
    //classOf[org.elasticsearch.xpack.ml.MachineLearning],
    //classOf[org.elasticsearch.xpack.security.Security],
    //classOf[org.elasticsearch.xpack.sql.plugin.SqlPlugin],
    //classOf[org.elasticsearch.xpack.indexlifecycle.IndexLifecycle],
    //      classOf[org.elasticsearch.join.ParentJoinPlugin]
    //      )

  
  def plugins = """
org.elasticsearch.transport.Netty4Plugin
org.elasticsearch.xpack.indexlifecycle.IndexLifecycle
org.elasticsearch.analysis.common.CommonAnalysisPlugin
org.elasticsearch.index.reindex.ReindexPlugin
org.elasticsearch.percolator.PercolatorPlugin
org.elasticsearch.xpack.graph.Graph
org.elasticsearch.ingest.common.IngestCommonPlugin
org.elasticsearch.xpack.rollup.Rollup
org.elasticsearch.painless.PainlessPlugin
org.elasticsearch.xpack.core.XPackPlugin
org.elasticsearch.xpack.ccr.Ccr
org.elasticsearch.plugin.repository.url.URLRepositoryPlugin
org.elasticsearch.xpack.monitoring.Monitoring
org.elasticsearch.search.aggregations.matrix.MatrixAggregationPlugin
org.elasticsearch.xpack.watcher.Watcher
org.elasticsearch.tribe.TribePlugin
org.elasticsearch.xpack.deprecation.Deprecation
org.elasticsearch.xpack.logstash.Logstash
org.elasticsearch.xpack.upgrade.Upgrade
org.elasticsearch.index.mapper.MapperExtrasPlugin
org.elasticsearch.xpack.sql.plugin.SqlPlugin
org.elasticsearch.script.mustache.MustachePlugin
org.elasticsearch.xpack.ml.MachineLearning
org.elasticsearch.script.expression.ExpressionPlugin
org.elasticsearch.index.rankeval.RankEvalPlugin
org.elasticsearch.xpack.security.Security
org.elasticsearch.join.ParentJoinPlugin  
  """.trim().split("\\s+\n")

}

