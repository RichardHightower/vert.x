/*
 *
 *  * Copyright (c) 2011-2016 The original author or authors
 *  * This project contains modified work from the Vert.x Project.
 *  * The Vert.x project Copyright is owned by Red Hat and/or the
 *  * original authors of the Vert.x project including Tim Fox, Julien Vet,
 *  * Norman Maurer, and many others.
 *  * We have left the original author tags on this MODIFIED COPY/FORK.
 *  *
 *  * Modified work is Copyright (c) 2015-2016 Rick Hightower and Geoff Chandler.
 *  * ------------------------------------------------------
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * and Apache License v2.0 which accompanies this distribution.
 *  *
 *  *     The Eclipse Public License is available at
 *  *     http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *     The Apache License v2.0 is available at
 *  *     http://www.opensource.org/licenses/apache2.0.php
 *  *
 *  * You may elect to redistribute this code under either of these licenses.
 *
 */

package io.advantageous.conekt.eventbus.impl;

import io.advantageous.conekt.eventbus.MessageCodec;
import io.advantageous.conekt.eventbus.impl.codecs.*;
import io.advantageous.conekt.buffer.Buffer;
import io.advantageous.conekt.eventbus.ReplyException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CodecManager {

    // The standard message codecs

    public static final MessageCodec<String, String> BULLSHIT = new PingMessageCodec();
    public static final MessageCodec<String, String> PING_MESSAGE_CODEC = new PingMessageCodec();
    public static final MessageCodec<String, String> NULL_MESSAGE_CODEC = new NullMessageCodec();
    public static final MessageCodec<String, String> STRING_MESSAGE_CODEC = new StringMessageCodec();
    public static final MessageCodec<Buffer, Buffer> BUFFER_MESSAGE_CODEC = new BufferMessageCodec();
    public static final MessageCodec<byte[], byte[]> BYTE_ARRAY_MESSAGE_CODEC = new ByteArrayMessageCodec();
    public static final MessageCodec<Integer, Integer> INT_MESSAGE_CODEC = new IntMessageCodec();
    public static final MessageCodec<Long, Long> LONG_MESSAGE_CODEC = new LongMessageCodec();
    public static final MessageCodec<Float, Float> FLOAT_MESSAGE_CODEC = new FloatMessageCodec();
    public static final MessageCodec<Double, Double> DOUBLE_MESSAGE_CODEC = new DoubleMessageCodec();
    public static final MessageCodec<Boolean, Boolean> BOOLEAN_MESSAGE_CODEC = new BooleanMessageCodec();
    public static final MessageCodec<Short, Short> SHORT_MESSAGE_CODEC = new ShortMessageCodec();
    public static final MessageCodec<Character, Character> CHAR_MESSAGE_CODEC = new CharMessageCodec();
    public static final MessageCodec<Byte, Byte> BYTE_MESSAGE_CODEC = new ByteMessageCodec();
    public static final MessageCodec<ReplyException, ReplyException> REPLY_EXCEPTION_MESSAGE_CODEC = new ReplyExceptionMessageCodec();

    private final MessageCodec[] systemCodecs;
    private final ConcurrentMap<String, MessageCodec> userCodecMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class, MessageCodec> defaultCodecMap = new ConcurrentHashMap<>();

    public CodecManager() {
        this.systemCodecs = codecs(NULL_MESSAGE_CODEC, PING_MESSAGE_CODEC, STRING_MESSAGE_CODEC, BUFFER_MESSAGE_CODEC,
                BULLSHIT, BULLSHIT,
                BYTE_ARRAY_MESSAGE_CODEC, INT_MESSAGE_CODEC, LONG_MESSAGE_CODEC, FLOAT_MESSAGE_CODEC, DOUBLE_MESSAGE_CODEC,
                BOOLEAN_MESSAGE_CODEC, SHORT_MESSAGE_CODEC, CHAR_MESSAGE_CODEC, BYTE_MESSAGE_CODEC, REPLY_EXCEPTION_MESSAGE_CODEC);
    }

    public MessageCodec lookupCodec(Object body, String codecName) {
        MessageCodec codec;
        if (codecName != null) {
            codec = userCodecMap.get(codecName);
            if (codec == null) {
                throw new IllegalArgumentException("No message codec for name: " + codecName);
            }
        } else if (body == null) {
            codec = NULL_MESSAGE_CODEC;
        } else if (body instanceof String) {
            codec = STRING_MESSAGE_CODEC;
        } else if (body instanceof Buffer) {
            codec = BUFFER_MESSAGE_CODEC;
        } else if (body instanceof byte[]) {
            codec = BYTE_ARRAY_MESSAGE_CODEC;
        } else if (body instanceof Integer) {
            codec = INT_MESSAGE_CODEC;
        } else if (body instanceof Long) {
            codec = LONG_MESSAGE_CODEC;
        } else if (body instanceof Float) {
            codec = FLOAT_MESSAGE_CODEC;
        } else if (body instanceof Double) {
            codec = DOUBLE_MESSAGE_CODEC;
        } else if (body instanceof Boolean) {
            codec = BOOLEAN_MESSAGE_CODEC;
        } else if (body instanceof Short) {
            codec = SHORT_MESSAGE_CODEC;
        } else if (body instanceof Character) {
            codec = CHAR_MESSAGE_CODEC;
        } else if (body instanceof Byte) {
            codec = BYTE_MESSAGE_CODEC;
        } else if (body instanceof ReplyException) {
            codec = REPLY_EXCEPTION_MESSAGE_CODEC;
        } else {
            codec = defaultCodecMap.get(body.getClass());
            if (codec == null) {
                throw new IllegalArgumentException("No message codec for type: " + body.getClass());
            }
        }
        return codec;
    }

    public MessageCodec getCodec(String codecName) {
        return userCodecMap.get(codecName);
    }

    public void registerCodec(MessageCodec codec) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(codec.name(), "code.name()");
        checkSystemCodec(codec);
        if (userCodecMap.containsKey(codec.name())) {
            throw new IllegalStateException("Already a codec registered with name " + codec.name());
        }
        userCodecMap.put(codec.name(), codec);
    }

    public void unregisterCodec(String name) {
        Objects.requireNonNull(name);
        userCodecMap.remove(name);
    }

    public <T> void registerDefaultCodec(Class<T> clazz, MessageCodec<T, ?> codec) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(codec.name(), "code.name()");
        checkSystemCodec(codec);
        if (defaultCodecMap.containsKey(clazz)) {
            throw new IllegalStateException("Already a default codec registered for class " + clazz);
        }
        if (userCodecMap.containsKey(codec.name())) {
            throw new IllegalStateException("Already a codec registered with name " + codec.name());
        }
        defaultCodecMap.put(clazz, codec);
        userCodecMap.put(codec.name(), codec);
    }

    public void unregisterDefaultCodec(Class clazz) {
        Objects.requireNonNull(clazz);
        MessageCodec codec = defaultCodecMap.remove(clazz);
        if (codec != null) {
            userCodecMap.remove(codec.name());
        }
    }

    public MessageCodec[] systemCodecs() {
        return systemCodecs;
    }

    private void checkSystemCodec(MessageCodec codec) {
        if (codec.systemCodecID() != -1) {
            throw new IllegalArgumentException("Can't register a system codec");
        }
    }

    private MessageCodec[] codecs(MessageCodec... codecs) {
        MessageCodec[] arr = new MessageCodec[codecs.length];
        for (MessageCodec codec : codecs) {
            arr[codec.systemCodecID()] = codec;
        }
        return arr;
    }


}
