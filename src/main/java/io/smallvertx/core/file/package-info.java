/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/**
 * == Using the file system with Vert.x
 * <p>
 * The Vert.x {@link io.smallvertx.core.file.FileSystem} object provides many operations for manipulating the file system.
 * <p>
 * There is one file system object per Vert.x instance, and you obtain it with  {@link io.smallvertx.core.Vertx#fileSystem()}.
 * <p>
 * A blocking and a non blocking version of each operation is provided. The non blocking versions take a handler
 * which is called when the operation completes or an error occurs.
 * <p>
 * Here's an example of an asynchronous copy of a file:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#example1}
 * ----
 * The blocking versions are named `xxxBlocking` and return the results or throw exceptions directly. In many
 * cases, depending on the operating system and file system, some of the potentially blocking operations can return
 * quickly, which is why we provide them, but it's highly recommended that you test how long they take to return in your
 * particular application before using them from an event loop, so as not to break the Golden Rule.
 * <p>
 * Here's the copy using the blocking API:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#example2}
 * ----
 * <p>
 * Many operations exist to copy, move, truncate, chmod and many other file operations. We won't list them all here,
 * please consult the {@link io.smallvertx.core.file.FileSystem API docs} for the full list.
 * <p>
 * Let's see a couple of examples using asynchronous methods:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#asyncAPIExamples}
 * ----
 * <p>
 * === Asynchronous files
 * <p>
 * Vert.x provides an asynchronous file abstraction that allows you to manipulate a file on the file system.
 * <p>
 * You open an {@link io.smallvertx.core.file.AsyncFile AsyncFile} as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#example3}
 * ----
 * <p>
 * `AsyncFile` implements `ReadStream` and `WriteStream` so you can _pump_
 * files to and from other stream objects such as net sockets, http requests and responses, and WebSockets.
 * <p>
 * They also allow you to read and write directly to them.
 * <p>
 * ==== Random access writes
 * <p>
 * To use an `AsyncFile` for random access writing you use the
 * {@link io.smallvertx.core.file.AsyncFile#write(Buffer, long, Handler) write} method.
 * <p>
 * The parameters to the method are:
 * <p>
 * * `buffer`: the buffer to write.
 * * `position`: an integer position in the file where to write the buffer. If the position is greater or equal to the size
 * of the file, the file will be enlarged to accommodate the offset.
 * * `handler`: the result handler
 * <p>
 * Here is an example of random access writes:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#asyncFileWrite()}
 * ----
 * <p>
 * ==== Random access reads
 * <p>
 * To use an `AsyncFile` for random access reads you use the
 * {@link io.smallvertx.core.file.AsyncFile#read(Buffer, int, long, int, Handler) read}
 * method.
 * <p>
 * The parameters to the method are:
 * <p>
 * * `buffer`: the buffer into which the data will be read.
 * * `offset`: an integer offset into the buffer where the read data will be placed.
 * * `position`: the position in the file where to read data from.
 * * `length`: the number of bytes of data to read
 * * `handler`: the result handler
 * <p>
 * Here's an example of random access reads:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#asyncFileRead()}
 * ----
 * <p>
 * ==== Opening Options
 * <p>
 * When opening an `AsyncFile`, you pass an {@link io.smallvertx.core.file.OpenOptions OpenOptions} instance.
 * These options describe the behavior of the file access. For instance, you can configure the file permissions with the
 * {@link io.smallvertx.core.file.OpenOptions#setRead(boolean)}, {@link io.smallvertx.core.file.OpenOptions#setWrite(boolean)}
 * and {@link io.smallvertx.core.file.OpenOptions#setPerms(java.lang.String)} methods.
 * <p>
 * You can also configure the behavior if the open file already exists with
 * {@link io.smallvertx.core.file.OpenOptions#setCreateNew(boolean)} and
 * {@link io.smallvertx.core.file.OpenOptions#setTruncateExisting(boolean)}.
 * <p>
 * You can also mark the file to be deleted on
 * close or when the JVM is shutdown with {@link io.smallvertx.core.file.OpenOptions#setDeleteOnClose(boolean)}.
 * <p>
 * ==== Flushing data to underlying storage.
 * <p>
 * In the `OpenOptions`, you can enable/disable the automatic synchronisation of the content on every write using
 * {@link io.smallvertx.core.file.OpenOptions#setDsync(boolean)}. In that case, you can manually flush any writes from the OS
 * cache by calling the {@link io.smallvertx.core.file.AsyncFile#flush()} method.
 * <p>
 * This method can also be called with an handler which will be called when the flush is complete.
 * <p>
 * ==== Using AsyncFile as ReadStream and WriteStream
 * <p>
 * `AsyncFile` implements `ReadStream` and `WriteStream`. You can then
 * use them with a _pump_ to pump data to and from other read and write streams. For example, this would
 * copy the content to another `AsyncFile`:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.FileSystemExamples#asyncFilePump()}
 * ----
 * <p>
 * You can also use the _pump_ to write file content into HTTP responses, or more generally in any
 * `WriteStream`.
 * <p>
 * [[classpath]]
 * ==== Accessing files from the classpath
 * <p>
 * When vert.x cannot find the file on the filesystem it tries to resolve the
 * file from the class path. Note that classpath resource paths never start with
 * a `/`.
 * <p>
 * Due to the fact that Java does not offer async access to classpath
 * resources, the file is copied to the filesystem in a worker thread when the
 * classpath resource is accessed the very first time and served from there
 * asynchrously. When the same resource is accessed a second time, the file from
 * the filesystem is served directly from the filesystem. The original content
 * is served even if the classpath resource changes (e.g. in a development
 * system).
 * <p>
 * This caching behaviour can be disabled by setting the system
 * property `vertx.disableFileCaching` to `true`. The path where the files are
 * cached is `.vertx` by default and can be customized by setting the system
 * property `vertx.cacheDirBase`.
 * <p>
 * The whole classpath resolving feature can be disabled by setting the system
 * property `vertx.disableFileCPResolving` to `true`.
 * <p>
 * ==== Closing an AsyncFile
 * <p>
 * To close an `AsyncFile` call the {@link io.smallvertx.core.file.AsyncFile#close()} method. Closing is asynchronous and
 * if you want to be notified when the close has been completed you can specify a handler function as an argument.
 */
package io.smallvertx.core.file;

import io.smallvertx.core.Handler;
import io.smallvertx.core.buffer.Buffer;

