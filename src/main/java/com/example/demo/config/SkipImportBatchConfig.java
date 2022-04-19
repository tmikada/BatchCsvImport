package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.model.Employee;

@Configuration
public class SkipImportBatchConfig extends BaseConfig {

	/** Listener */
	@Autowired
	private SkipListener<Employee, Employee> employeeSkipListener;
	
//	@Autowired
//	private MyBatisBatchItemWriter<Employee> mybatisWriter;
	/** DataSource(JDBCで必須) */
	@Autowired
	private DataSource dataSource;

	/** insert-sql(JDBC用) */
	private static final String INSERT_EMPLOYEE_SQL = 
			"INSERT INTO employee (id, name, age, gender)"
			+ "VALUES (:id, :name, :age, :gender)";

	/** Writer(JDBC) */
	@Bean("jdbcSkipWriter")
	@StepScope
	public JdbcBatchItemWriter<Employee> jdbcWriter() {
		// Provider生成
		BeanPropertyItemSqlParameterSourceProvider<Employee> provider = 
				new BeanPropertyItemSqlParameterSourceProvider<>();
		
		// 設定
		return new JdbcBatchItemWriterBuilder<Employee>() // Builderの生成
				.itemSqlParameterSourceProvider(provider)
				.sql(INSERT_EMPLOYEE_SQL)
				.dataSource(this.dataSource)
				.build();
	}
	
	
	/** Stepの生成（Skip） */
	@Bean
	public Step csvImportSkipStep() {
		return this.stepBuilderFactory.get("CsvImportSkipStep")
				.<Employee, Employee>chunk(10)
				.reader(csvReader()).listener(this.readListener)
				.processor(genderConvertProcessor).listener(this.processListener)
				.writer(jdbcWriter())
				.faultTolerant()
				.skipLimit(Integer.MAX_VALUE) // スキップ最大件数
				.skip(RuntimeException.class) // 例外クラス
				.listener(this.employeeSkipListener)
				.build();
	}
	
	/** Jobの生成（Skip） */
	@Bean("SkipJob")
	public Job csvImportSkipJob() {
		return this.jobBuilderFactory.get("CsvImportSkipJob")
				.incrementer(new RunIdIncrementer())
				.start(csvImportSkipStep())
				.build();
	}
	
}
