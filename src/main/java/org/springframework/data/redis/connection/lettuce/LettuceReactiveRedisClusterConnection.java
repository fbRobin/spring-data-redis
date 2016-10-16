/*
 * Copyright 2016. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.redis.connection.lettuce;

import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;

import reactor.core.publisher.Flux;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public class LettuceReactiveRedisClusterConnection extends LettuceReactiveRedisConnection
		implements ReactiveRedisClusterConnection {

	public LettuceReactiveRedisClusterConnection(RedisClusterClient client) {
		super(client);
	}

	@Override
	public LettuceReactiveClusterKeyCommands keyCommands() {
		return new LettuceReactiveClusterKeyCommands(this);
	}

	@Override
	public LettuceReactiveClusterListCommands listCommands() {
		return new LettuceReactiveClusterListCommands(this);
	}

	@Override
	public LettuceReactiveClusterSetCommands setCommands() {
		return new LettuceReactiveClusterSetCommands(this);
	}

	@Override
	public LettuceReactiveClusterZSetCommands zSetCommands() {
		return new LettuceReactiveClusterZSetCommands(this);
	}

	@Override
	public LettuceReactiveClusterHyperLogLogCommands hyperLogLogCommands() {
		return new LettuceReactiveClusterHyperLogLogCommands(this);
	}

	@Override
	public LettuceReactiveClusterStringCommands stringCommands() {
		return new LettuceReactiveClusterStringCommands(this);
	}

	@Override
	public LettuceReactiveClusterGeoCommands geoCommands() {
		return new LettuceReactiveClusterGeoCommands(this);
	}

	@Override
	public LettuceReactiveClusterHashCommands hashCommands() {
		return new LettuceReactiveClusterHashCommands(this);
	}

	@Override
	public LettuceReactiveClusterNumberCommands numberCommands() {
		return new LettuceReactiveClusterNumberCommands(this);
	}

	@Override
	protected StatefulRedisClusterConnection<byte[], byte[]> getConnection() {

		if (!(super.getConnection() instanceof StatefulRedisClusterConnection)) {
			throw new IllegalArgumentException("o.O connection needs to be cluster compatible " + getConnection());
		}

		return (StatefulRedisClusterConnection) super.getConnection();
	}

	/**
	 * @param callback
	 * @return
	 */
	public <T> Flux<T> execute(RedisNode node, LettuceReactiveCallback<T> callback) {

		try {
			Assert.notNull(callback, "ReactiveCallback must not be null!");
			Assert.notNull(node, "Node must not be null!");
		} catch (IllegalArgumentException e) {
			return Flux.error(e);
		}

		return Flux.defer(() -> callback.doWithCommands(getCommands(node))).onErrorResumeWith(translateExecption());
	}

	protected RedisClusterReactiveCommands<byte[], byte[]> getCommands() {
		return getConnection().reactive();
	}

	protected RedisReactiveCommands<byte[], byte[]> getCommands(RedisNode node) {

		if (!(getConnection() instanceof StatefulRedisClusterConnection)) {
			throw new IllegalArgumentException("o.O connection needs to be cluster compatible " + getConnection());
		}

		if (StringUtils.hasText(node.getId())) {
			return ((StatefulRedisClusterConnection) getConnection()).getConnection(node.getId()).reactive();
		}

		return ((StatefulRedisClusterConnection) getConnection()).getConnection(node.getHost(), node.getPort()).reactive();
	}
}
