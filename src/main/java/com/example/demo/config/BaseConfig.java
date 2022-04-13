package com.example.demo.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import com.example.demo.domain.model.Employee;
import com.example.demo.property.SampleProperty;

@EnableBatchProcessing
public abstract class BaseConfig {
	
	/** JobBuilderのFactoryクラス */
	@Autowired
	protected JobBuilderFactory jobBuilderFactory;
	
	/** StepBuilderのFactoryクラス */
	@Autowired
	protected StepBuilderFactory stepBuilderFactory;

	/** データの存在チェックするProcessor */
	@Autowired
	@Qualifier("ExistsCheckProcessor")
	protected ItemProcessor<Employee, Employee> existsCheckProcessor;
	
	/** 性別の文字列を数値に変換するProcessor */
	@Autowired
	@Qualifier("GenderConvertProcessor")
	protected ItemProcessor<Employee, Employee> genderConvertProcessor;
	
	@Autowired
	protected ItemReadListener<Employee> readListener;
	
	@Autowired
	protected ItemProcessListener<Employee, Employee> processListener;
	
	@Autowired
	protected ItemWriteListener<Employee> writeListener;

	@Autowired
	protected SampleProperty property;
	
	/** csvファイルのReader */
	@Bean
	@StepScope
	public FlatFileItemReader<Employee> csvReader() {
		
		// CSVのカラムに付ける名前
		String[] nameArray = new String[] {"id","name","age","genderString"};
		
		// ファイル読み込み設定
		return new FlatFileItemReaderBuilder<Employee>()
				.name("employeeCsvReader") // 名前
				.resource(new ClassPathResource(property.getCsvPath())) // ファイルパス
				.linesToSkip(1) // スキップする行数
				.encoding(StandardCharsets.UTF_8.name()) // 文字コード
				.delimited() // DelimitedBuilderを取得
					.names(nameArray) // ファイルのカラムに付ける名前
					.fieldSetMapper(new BeanWrapperFieldSetMapper<Employee>() {
						{
							// ファイルの値を指定クラスにバインド
							setTargetType(Employee.class);
						}
					})
				.build(); // readerの生成
		
	}

	/** 複数のProcessor */
	@Bean
	@StepScope
	public ItemProcessor<Employee,Employee> compositeProcessor() {
		CompositeItemProcessor<Employee,Employee> compositeProcessor = new CompositeItemProcessor<>();
		
		// ProcessorList
		compositeProcessor.setDelegates(Arrays.asList(validationProcessor(), 
				existsCheckProcessor, 
				this.genderConvertProcessor));
		return compositeProcessor;
	}

	/** ValidationのProcessor */
	@Bean
	@StepScope
	public BeanValidatingItemProcessor<Employee> validationProcessor() {
		BeanValidatingItemProcessor<Employee> validationProcessor = new BeanValidatingItemProcessor<Employee>();
		
		// true: skip
		// false: throw ValidationException
		validationProcessor.setFilter(true);
		
		return validationProcessor;
	}
}
