package com.example.demo.listener;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

import com.example.demo.domain.model.Employee;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProcessListener implements ItemProcessListener<Employee, Employee> {

	@Override
	public void beforeProcess(Employee item) {
		// do nothing
	}

	@Override
	public void afterProcess(Employee item, Employee result) {
		// do nothing
	}

	@Override
	public void onProcessError(Employee item, Exception e) {
		log.error("ProcessError: errorMessage", e.getMessage(), e);
	}

}
