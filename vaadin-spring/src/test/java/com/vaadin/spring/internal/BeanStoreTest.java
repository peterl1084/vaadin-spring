/*
 * Copyright 2015 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.spring.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectFactory;

public class BeanStoreTest {

	String beanName1 = "TestBean1";
	String beanName2 = "TestBean2";
	String beanStoreName = "TestBeanStore";
	Object bean1 = new Object();
	Object bean2 = new Object();
	ObjectFactory<Object> objFactory1;
	ObjectFactory<Object> objFactory2;
	BeanStore beanStore = new BeanStore(this.beanStoreName);

	@SuppressWarnings("unchecked")
	@Before
	public void initBeanFactory() {
		// Create dummy bean factories that return a simple bean
		this.objFactory1 = mock(ObjectFactory.class);
		when(this.objFactory1.getObject()).thenReturn(this.bean1);
		this.objFactory2 = mock(ObjectFactory.class);
		when(this.objFactory2.getObject()).thenReturn(this.bean2);
	}

	@Test
	public void testCreateBean() {
		assertSame(this.bean1, this.beanStore.create(this.beanName1, this.objFactory1));
	}

	@Test
	public void testGetBean() {
		assertSame(this.bean1, this.beanStore.get(this.beanName1, this.objFactory1));

		assertNotSame(this.bean2, this.beanStore.get(this.beanName1, this.objFactory1));

		assertNotSame(this.bean1, this.beanStore.get(this.beanName2, this.objFactory2));
	}

	@Test
	public void testGetConsistent() {
		// Make sure the same name gives the same instance
		assertSame(	this.beanStore.get(this.beanName1, this.objFactory1),
					this.beanStore.get(this.beanName1, this.objFactory1));
	}

	@Test
	public void testGetSameInstance() {

		// First time should at most create the factory once
		this.beanStore.get(this.beanName1, this.objFactory1);

		// Make sure it will not be created more than once
		this.beanStore.get(this.beanName1, this.objFactory1);

		verify(this.objFactory1, atMost(1)).getObject();
	}

	@Test
	public void testRemoveBean() {
		// Make sure to create a new bean if not already there
		this.beanStore.get(this.beanName1, this.objFactory1);

		// Make sure the bean is removed
		assertSame(this.bean1, this.beanStore.remove(this.beanName1));

		// Make sure it's already removed
		assertNull(this.beanStore.remove(this.beanName1));
	}

	@Test
	public void testRegisterDestructionCallbackAndDestroy() {

		Runnable destructionCallback = mock(Runnable.class);

		this.beanStore.registerDestructionCallback(this.beanStoreName, destructionCallback);

		// If registered it will be destroyed
		this.beanStore.destroy();

		// Make sure destructionCallback won't run again
		this.beanStore.destroy();

		// Make sure destroy() ran the registered destructionCallback once
		verify(destructionCallback).run();
	}

	@Test
	public void testDestroyClearStore() {

		// Make sure to create a new bean if not already there
		this.beanStore.get(this.beanName1, this.objFactory1);

		this.beanStore.destroy();

		// The bean should not be there anymore
		assertNull(this.beanStore.remove(this.beanName1));
	}

	@Test
	public void testToStringConsistent() {
		// Make sure the format is always the same
		assertEquals(this.beanStore.toString(), this.beanStore.toString());
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}
}
