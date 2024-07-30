package beyond.ordersystem.common.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    public String host;

    @Value("${spring.redis.port}")
    public int port;


    @Bean
    @Qualifier("2")
    public RedisConnectionFactory redisConnectionFactory() {
        // connection 정보 넣기

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // 1번 db 사용 - redis에서 select 1로 확인
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);

    }


    /**
     * redisTemplate 정의
     * redisTemplate는 redis와 상호작용 할 때 redis key, value의 형식을 정의
     * 위에서의 연결정보가 아래 파라미터 redisConnectionFactory에 주입이 됨
     */
    @Bean
    @Qualifier("2")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("2") RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // string 형태를 직렬화 시키게따 (java에 string으로 들어가게)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 제이슨 직렬화 툴 세팅
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 주입받은 connection을 넣어줌
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * redis로 동시성 해결
     */
    @Bean
    @Qualifier("3")
    public RedisConnectionFactory stockFactory() {
        // connection 정보 넣기

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // 2번 db 사용 - redis에서 select 2로 확인
        configuration.setDatabase(2);
        return new LettuceConnectionFactory(configuration);

    }

    @Bean
    @Qualifier("3")
    public RedisTemplate<String, Object> stockRedisTemplate(@Qualifier("3") RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // string 형태를 직렬화 시키게따 (java에 string으로 들어가게)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 제이슨 직렬화 툴 세팅
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 주입받은 connection을 넣어줌
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * redisTemplate를 불러다가 .opsForValue().set(key,value)
     * redisTemplate.opsForValue().get(key)
     * redisTemplate.opsForValue().increment 또는 decrement
     * => redisTemplate를 통해 메서드가 제공됨
     */

}
