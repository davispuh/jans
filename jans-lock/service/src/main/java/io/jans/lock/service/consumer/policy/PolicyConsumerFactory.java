/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2023, Janssen Project
 */

package io.jans.lock.service.consumer.policy;

import org.slf4j.Logger;

import io.jans.lock.model.config.AppConfiguration;
import io.jans.service.cdi.async.Asynchronous;
import io.jans.service.cdi.event.ApplicationInitialized;
import io.jans.service.cdi.event.ConfigurationUpdate;
import io.jans.service.cdi.qualifier.Implementation;
import io.jans.service.policy.consumer.PolicyConsumer;
import io.jans.util.StringHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * Message consumer factory
 *
 * @author Yuriy Movchan Date: 12/20/2023
 */
@ApplicationScoped
public class PolicyConsumerFactory {

	@Inject
	private Logger log;

    @Inject
	private AppConfiguration appConfiguration;

	@Inject
	@Implementation
	private Instance<PolicyConsumer> policyConsumerProviderInstances;

	private boolean appStarted = false;

	public void init(@Observes @ApplicationInitialized(ApplicationScoped.class) Object init) {
        this.appStarted  = true;
	}

	@Asynchronous
    public void configurationUpdateEvent(@Observes @ConfigurationUpdate AppConfiguration appConfiguration) {
		if (!appStarted) {
			return;
		}

		recreatePolicyConsumer();
	}

	private void recreatePolicyConsumer() {
		// Force to create new bean
		for (PolicyConsumer policyConsumer : policyConsumerProviderInstances) {
			policyConsumerProviderInstances.destroy(policyConsumer);
	        log.info("Recreated policyConsumer instance '{}'", policyConsumer);
		}
	}

	@Produces
	@ApplicationScoped
	public PolicyConsumer producePolicyConsumer() {
		String policyConsumerType = appConfiguration.getPolicyConsumerType();
		PolicyConsumer policyConsumer = buildPolicyConsumer(policyConsumerType);
		
		return policyConsumer;
	}

	private PolicyConsumer buildPolicyConsumer(String policyConsumerType) {
		for (PolicyConsumer policyConsumer : policyConsumerProviderInstances) {
			String serviceMessageConsumerType = policyConsumer.getPolicyConsumerType();
			if (StringHelper.equalsIgnoreCase(serviceMessageConsumerType, policyConsumerType)) {
				return policyConsumer;
			}
		}
		
		log.error("Failed to find policy consumer with type '{}'. Using null policy consumer", policyConsumerType);
		return policyConsumerProviderInstances.select(NullPolicyConsumer.class).get();
	}

}
