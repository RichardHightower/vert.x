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

/**
 * == Writing HTTP servers and clients
 * <p>
 * Vert.x allows you to easily write non blocking HTTP clients and servers.
 * <p>
 * === Creating an HTTP Server
 * <p>
 * The simplest way to create an HTTP server, using all default options is as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example1}
 * ----
 * <p>
 * === Configuring an HTTP server
 * <p>
 * If you don't want the default, a server can be configured by passing in a {@link io.advantageous.conekt.http.HttpServerOptions}
 * instance when creating it:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example2}
 * ----
 * <p>
 * === Start the Server Listening
 * <p>
 * To tell the server to listen for incoming requests you use one of the {@link io.advantageous.conekt.http.HttpServer#listen}
 * alternatives.
 * <p>
 * To tell the server to listen at the host and port as specified in the options:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example3}
 * ----
 * <p>
 * Or to specify the host and port in the call to listen, ignoring what is configured in the options:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example4}
 * ----
 * <p>
 * The default host is `0.0.0.0` which means 'listen on all available addresses' and the default port is `80`.
 * <p>
 * The actual bind is asynchronous so the server might not actually be listening until some time *after* the call to
 * listen has returned.
 * <p>
 * If you want to be notified when the server is actually listening you can provide a handler to the `listen` call.
 * For example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example5}
 * ----
 * <p>
 * === Getting notified of incoming requests
 * <p>
 * To be notified when a request arrives you need to set a {@link io.advantageous.conekt.http.HttpServer#requestHandler}:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example6}
 * ----
 * <p>
 * === Handling requests
 * <p>
 * When a request arrives, the request handler is called passing in an instance of {@link io.advantageous.conekt.http.HttpServerRequest}.
 * This object represents the server side HTTP request.
 * <p>
 * The handler is called when the headers of the request have been fully read.
 * <p>
 * If the request contains a body, that body will arrive at the server some time after the request handler has been called.
 * <p>
 * The server request object allows you to retrieve the {@link io.advantageous.conekt.http.HttpServerRequest#uri},
 * {@link io.advantageous.conekt.http.HttpServerRequest#path}, {@link io.advantageous.conekt.http.HttpServerRequest#params} and
 * {@link io.advantageous.conekt.http.HttpServerRequest#headers}, amongst other things.
 * <p>
 * Each server request object is associated with one server response object. You use
 * {@link io.advantageous.conekt.http.HttpServerRequest#response} to get a reference to the {@link io.advantageous.conekt.http.HttpServerResponse}
 * object.
 * <p>
 * Here's a simple example of a server handling a request and replying with "hello world" to it.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example7_1}
 * ----
 * <p>
 * ==== Request version
 * <p>
 * The version of HTTP specified in the request can be retrieved with {@link io.advantageous.conekt.http.HttpServerRequest#version}
 * <p>
 * ==== Request method
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#method} to retrieve the HTTP method of the request.
 * (i.e. whether it's GET, POST, PUT, DELETE, HEAD, OPTIONS, etc).
 * <p>
 * ==== Request URI
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#uri} to retrieve the URI of the request.
 * <p>
 * Note that this is the actual URI as passed in the HTTP request, and it's almost always a relative URI.
 * <p>
 * The URI is as defined in http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html[Section 5.1.2 of the HTTP specification - Request-URI]
 * <p>
 * ==== Request path
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#path} to return the path part of the URI
 * <p>
 * For example, if the request URI was:
 * <p>
 * a/b/c/page.html?param1=abc&param2=xyz
 * <p>
 * Then the path would be
 * <p>
 * /a/b/c/page.html
 * <p>
 * ==== Request query
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#query} to return the query part of the URI
 * <p>
 * For example, if the request URI was:
 * <p>
 * a/b/c/page.html?param1=abc&param2=xyz
 * <p>
 * Then the query would be
 * <p>
 * param1=abc&param2=xyz
 * <p>
 * ==== Request headers
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#headers} to return the headers of the HTTP request.
 * <p>
 * This returns an instance of {@link io.advantageous.conekt.MultiMap} - which is like a normal Map or Hash but allows multiple
 * values for the same key - this is because HTTP allows multiple header values with the same key.
 * <p>
 * It also has case-insensitive keys, that means you can do the following:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example8}
 * ----
 * <p>
 * ==== Request parameters
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerRequest#params} to return the parameters of the HTTP request.
 * <p>
 * Just like {@link io.advantageous.conekt.http.HttpServerRequest#headers} this returns an instance of {@link io.advantageous.conekt.MultiMap}
 * as there can be more than one parameter with the same name.
 * <p>
 * Request parameters are sent on the request URI, after the path. For example if the URI was:
 * <p>
 * /page.html?param1=abc&param2=xyz
 * <p>
 * Then the parameters would contain the following:
 * <p>
 * ----
 * param1: 'abc'
 * param2: 'xyz
 * ----
 * <p>
 * Note that these request parameters are retrieved from the URL of the request. If you have form attributes that
 * have been sent as part of the submission of an HTML form submitted in the body of a `multi-part/form-data` request
 * then they will not appear in the params here.
 * <p>
 * ==== Remote address
 * <p>
 * The address of the sender of the request can be retrieved with {@link io.advantageous.conekt.http.HttpServerRequest#remoteAddress}.
 * <p>
 * ==== Absolute URI
 * <p>
 * The URI passed in an HTTP request is usually relative. If you wish to retrieve the absolute URI corresponding
 * to the request, you can get it with {@link io.advantageous.conekt.http.HttpServerRequest#absoluteURI}
 * <p>
 * ==== End handler
 * <p>
 * The {@link io.advantageous.conekt.http.HttpServerRequest#endHandler} of the request is invoked when the entire request,
 * including any body has been fully read.
 * <p>
 * ==== Reading Data from the Request Body
 * <p>
 * Often an HTTP request contains a body that we want to read. As previously mentioned the request handler is called
 * when just the headers of the request have arrived so the request object does not have a body at that point.
 * <p>
 * This is because the body may be very large (e.g. a file upload) and we don't generally want to buffer the entire
 * body in memory before handing it to you, as that could cause the server to exhaust available memory.
 * <p>
 * To receive the body, you can use the {@link io.advantageous.conekt.http.HttpServerRequest#handler}  on the request,
 * this will get called every time a chunk of the request body arrives. Here's an example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example9}
 * ----
 * <p>
 * The object passed into the handler is a {@link io.advantageous.conekt.buffer.Buffer}, and the handler can be called
 * multiple times as data arrives from the network, depending on the size of the body.
 * <p>
 * In some cases (e.g. if the body is small) you will want to aggregate the entire body in memory, so you could do
 * the aggregation yourself as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example10}
 * ----
 * <p>
 * This is such a common case, that Vert.x provides a {@link io.advantageous.conekt.http.HttpServerRequest#bodyHandler} to do this
 * for you. The body handler is called once when all the body has been received:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example11}
 * ----
 * <p>
 * ==== Pumping requests
 * <p>
 * The request object is a {@link io.advantageous.conekt.streams.ReadStream} so you can pump the request body to any
 * {@link io.advantageous.conekt.streams.WriteStream} instance.
 * <p>
 * See the chapter on <<streams, streams and pumps>> for a detailed explanation.
 * <p>
 * ==== Handling HTML forms
 * <p>
 * HTML forms can be submitted with either a content type of `application/x-www-form-urlencoded` or `multipart/form-data`.
 * <p>
 * For url encoded forms, the form attributes are encoded in the url, just like normal query parameters.
 * <p>
 * For multi-part forms they are encoded in the request body, and as such are not available until the entire body
 * has been read from the wire.
 * <p>
 * Multi-part forms can also contain file uploads.
 * <p>
 * If you want to retrieve the attributes of a multi-part form you should tell Vert.x that you expect to receive
 * such a form *before* any of the body is read by calling {@link io.advantageous.conekt.http.HttpServerRequest#setExpectMultipart}
 * with true, and then you should retrieve the actual attributes using {@link io.advantageous.conekt.http.HttpServerRequest#formAttributes}
 * once the entire body has been read:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example12}
 * ----
 * <p>
 * ==== Handling form file uploads
 * <p>
 * Vert.x can also handle file uploads which are encoded in a multi-part request body.
 * <p>
 * To receive file uploads you tell Vert.x to expect a multi-part form and set an
 * {@link io.advantageous.conekt.http.HttpServerRequest#uploadHandler} on the request.
 * <p>
 * This handler will be called once for every
 * upload that arrives on the server.
 * <p>
 * The object passed into the handler is a {@link io.advantageous.conekt.http.HttpServerFileUpload} instance.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example13}
 * ----
 * <p>
 * File uploads can be large we don't provide the entire upload in a single buffer as that might result in memory
 * exhaustion, instead, the upload data is received in chunks:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example14}
 * ----
 * <p>
 * The upload object is a {@link io.advantageous.conekt.streams.ReadStream} so you can pump the request body to any
 * {@link io.advantageous.conekt.streams.WriteStream} instance. See the chapter on <<streams, streams and pumps>> for a
 * detailed explanation.
 * <p>
 * If you just want to upload the file to disk somewhere you can use {@link io.advantageous.conekt.http.HttpServerFileUpload#streamToFileSystem}:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example15}
 * ----
 * <p>
 * WARNING: Make sure you check the filename in a production system to avoid malicious clients uploading files
 * to arbitrary places on your filesystem. See <<Security notes, security notes>> for more information.
 * <p>
 * === Sending back responses
 * <p>
 * The server response object is an instance of {@link io.advantageous.conekt.http.HttpServerResponse} and is obtained from the
 * request with {@link io.advantageous.conekt.http.HttpServerRequest#response}.
 * <p>
 * You use the response object to write a response back to the HTTP client.
 * <p>
 * ==== Setting status code and message
 * <p>
 * The default HTTP status code for a response is `200`, representing `OK`.
 * <p>
 * Use {@link io.advantageous.conekt.http.HttpServerResponse#setStatusCode} to set a different code.
 * <p>
 * You can also specify a custom status message with {@link io.advantageous.conekt.http.HttpServerResponse#setStatusMessage}.
 * <p>
 * If you don't specify a status message, the default one corresponding to the status code will be used.
 * <p>
 * ==== Writing HTTP responses
 * <p>
 * To write data to an HTTP response, you use one the {@link io.advantageous.conekt.http.HttpServerResponse#write} operations.
 * <p>
 * These can be invoked multiple times before the response is ended. They can be invoked in a few ways:
 * <p>
 * With a single buffer:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example16}
 * ----
 * <p>
 * With a string. In this case the string will encoded using UTF-8 and the result written to the wire.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example17}
 * ----
 * <p>
 * With a string and an encoding. In this case the string will encoded using the specified encoding and the
 * result written to the wire.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example18}
 * ----
 * <p>
 * Writing to a response is asynchronous and always returns immediately after the write has been queued.
 * <p>
 * If you are just writing a single string or buffer to the HTTP response you can write it and end the response in a
 * single call to the {@link io.advantageous.conekt.http.HttpServerResponse#end(String)}
 * <p>
 * The first call to write results in the response header being being written to the response. Consequently, if you are
 * not using HTTP chunking then you must set the `Content-Length` header before writing to the response, since it will
 * be too late otherwise. If you are using HTTP chunking you do not have to worry.
 * <p>
 * ==== Ending HTTP responses
 * <p>
 * Once you have finished with the HTTP response you should {@link io.advantageous.conekt.http.HttpServerResponse#end} it.
 * <p>
 * This can be done in several ways:
 * <p>
 * With no arguments, the response is simply ended.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example19}
 * ----
 * <p>
 * It can also be called with a string or buffer in the same way `write` is called. In this case it's just the same as
 * calling write with a string or buffer followed by calling end with no arguments. For example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example20}
 * ----
 * <p>
 * ==== Closing the underlying connection
 * <p>
 * You can close the underlying TCP connection with {@link io.advantageous.conekt.http.HttpServerResponse#close}.
 * <p>
 * Non keep-alive connections will be automatically closed by Vert.x when the response is ended.
 * <p>
 * Keep-alive connections are not automatically closed by Vert.x by default. If you want keep-alive connections to be
 * closed after an idle time, then you configure {@link io.advantageous.conekt.http.HttpServerOptions#setIdleTimeout}.
 * <p>
 * ==== Setting response headers
 * <p>
 * HTTP response headers can be added to the response by adding them directly to the
 * {@link io.advantageous.conekt.http.HttpServerResponse#headers}:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example21}
 * ----
 * <p>
 * Or you can use {@link io.advantageous.conekt.http.HttpServerResponse#putHeader}
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example22}
 * ----
 * <p>
 * Headers must all be added before any parts of the response body are written.
 * <p>
 * ==== Chunked HTTP responses and trailers
 * <p>
 * Vert.x supports http://en.wikipedia.org/wiki/Chunked_transfer_encoding[HTTP Chunked Transfer Encoding].
 * <p>
 * This allows the HTTP response body to be written in chunks, and is normally used when a large response body is
 * being streamed to a client and the total size is not known in advance.
 * <p>
 * You put the HTTP response into chunked mode as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example23}
 * ----
 * <p>
 * Default is non-chunked. When in chunked mode, each call to one of the {@link io.advantageous.conekt.http.HttpServerResponse#write}
 * methods will result in a new HTTP chunk being written out.
 * <p>
 * When in chunked mode you can also write HTTP response trailers to the response. These are actually written in
 * the final chunk of the response.
 * <p>
 * To add trailers to the response, add them directly to the {@link io.advantageous.conekt.http.HttpServerResponse#trailers}.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example24}
 * ----
 * <p>
 * Or use {@link io.advantageous.conekt.http.HttpServerResponse#putTrailer}.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example25}
 * ----
 * <p>
 * ==== Serving files directly from disk or the classpath
 * <p>
 * If you were writing a web server, one way to serve a file from disk would be to open it as an {@link io.advantageous.conekt.file.AsyncFile}
 * and pump it to the HTTP response.
 * <p>
 * Or you could load it it one go using {@link io.advantageous.conekt.file.FileSystem#readFile} and write it straight to the response.
 * <p>
 * Alternatively, Vert.x provides a method which allows you to serve a file from disk or the filesystem to an HTTP response
 * in one operation.
 * Where supported by the underlying operating system this may result in the OS directly transferring bytes from the
 * file to the socket without being copied through user-space at all.
 * <p>
 * This is done by using {@link io.advantageous.conekt.http.HttpServerResponse#sendFile}, and is usually more efficient for large
 * files, but may be slower for small files.
 * <p>
 * Here's a very simple web server that serves files from the file system using sendFile:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26}
 * ----
 * <p>
 * Sending a file is asynchronous and may not complete until some time after the call has returned. If you want to
 * be notified when the file has been writen you can use {@link io.advantageous.conekt.http.HttpServerResponse#sendFile(String, Handler)}
 * <p>
 * Please see the chapter about <<classpath, serving files from the classpath>> for restrictions about the classpath resolution or disabling it.
 * <p>
 * NOTE: If you use `sendFile` while using HTTPS it will copy through user-space, since if the kernel is copying data
 * directly from disk to socket it doesn't give us an opportunity to apply any encryption.
 * <p>
 * WARNING: If you're going to write web servers directly using Vert.x be careful that users cannot exploit the
 * path to access files outside the directory from which you want to serve them or the classpath It may be safer instead to use
 * Vert.x Web.
 * <p>
 * When there is a need to serve just a segment of a file, say starting from a given byte, you can achieve this by doing:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26b}
 * ----
 * <p>
 * You are not required to supply the length if you want to send a file starting from an offset until the end, in this
 * case you can just do:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26c}
 * ----
 * <p>
 * ==== Pumping responses
 * <p>
 * The server response is a {@link io.advantageous.conekt.streams.WriteStream} instance so you can pump to it from any
 * {@link io.advantageous.conekt.streams.ReadStream}, e.g. {@link io.advantageous.conekt.file.AsyncFile}, {@link io.advantageous.conekt.net.NetSocket},
 * {@link io.advantageous.conekt.http.WebSocket} or {@link io.advantageous.conekt.http.HttpServerRequest}.
 * <p>
 * Here's an example which echoes the request body back in the response for any PUT methods.
 * It uses a pump for the body, so it will work even if the HTTP request body is much larger than can fit in memory
 * at any one time:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example27}
 * ----
 * <p>
 * === HTTP Compression
 * <p>
 * Vert.x comes with support for HTTP Compression out of the box.
 * <p>
 * This means you are able to automatically compress the body of the responses before they are sent back to the client.
 * <p>
 * If the client does not support HTTP compression the responses are sent back without compressing the body.
 * <p>
 * This allows to handle Client that support HTTP Compression and those that not support it at the same time.
 * <p>
 * To enable compression use can configure it with {@link io.advantageous.conekt.http.HttpServerOptions#setCompressionSupported}.
 * <p>
 * By default compression is not enabled.
 * <p>
 * When HTTP compression is enabled the server will check if the client includes an `Accept-Encoding` header which
 * includes the supported compressions. Commonly used are deflate and gzip. Both are supported by Vert.x.
 * <p>
 * If such a header is found the server will automatically compress the body of the response with one of the supported
 * compressions and send it back to the client.
 * <p>
 * Be aware that compression may be able to reduce network traffic but is more CPU-intensive.
 * <p>
 * === Creating an HTTP client
 * <p>
 * You create an {@link io.advantageous.conekt.http.HttpClient} instance with default options as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example28}
 * ----
 * <p>
 * If you want to configure options for the client, you create it as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example29}
 * ----
 * <p>
 * === Making requests
 * <p>
 * The http client is very flexible and there are various ways you can make requests with it.
 * <p>
 * <p>
 * Often you want to make many requests to the same host/port with an http client. To avoid you repeating the host/port
 * every time you make a request you can configure the client with a default host/port:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example30}
 * ----
 * <p>
 * Alternatively if you find yourself making lots of requests to different host/ports with the same client you can
 * simply specify the host/port when doing the request.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example31}
 * ----
 * <p>
 * Both methods of specifying host/port are supported for all the different ways of making requests with the client.
 * <p>
 * ==== Simple requests with no request body
 * <p>
 * Often, you'll want to make HTTP requests with no request body. This is usually the case with HTTP GET, OPTIONS and
 * HEAD requests.
 * <p>
 * The simplest way to do this with the Vert.x http client is using the methods prefixed with `Now`. For example
 * {@link io.advantageous.conekt.http.HttpClient#getNow}.
 * <p>
 * These methods create the http request and send it in a single method call and allow you to provide a handler that will be
 * called with the http response when it comes back.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example32}
 * ----
 * <p>
 * ==== Writing general requests
 * <p>
 * At other times you don't know the request method you want to send until run-time. For that use case we provide
 * general purpose request methods such as {@link io.advantageous.conekt.http.HttpClient#request} which allow you to specify
 * the HTTP method at run-time:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example33}
 * ----
 * <p>
 * ==== Writing request bodies
 * <p>
 * Sometimes you'll want to write requests which have a body, or perhaps you want to write headers to a request
 * before sending it.
 * <p>
 * To do this you can call one of the specific request methods such as {@link io.advantageous.conekt.http.HttpClient#post} or
 * one of the general purpose request methods such as {@link io.advantageous.conekt.http.HttpClient#request}.
 * <p>
 * These methods don't send the request immediately, but instead return an instance of {@link io.advantageous.conekt.http.HttpClientRequest}
 * which can be used to write to the request body or write headers.
 * <p>
 * Here are some examples of writing a POST request with a body:
 * m
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example34}
 * ----
 * <p>
 * Methods exist to write strings in UTF-8 encoding and in any specific encoding and to write buffers:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example35}
 * ----
 * <p>
 * If you are just writing a single string or buffer to the HTTP request you can write it and end the request in a
 * single call to the `end` function.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example36}
 * ----
 * <p>
 * When you're writing to a request, the first call to `write` will result in the request headers being written
 * out to the wire.
 * <p>
 * The actual write is asynchronous and might not occur until some time after the call has returned.
 * <p>
 * Non-chunked HTTP requests with a request body require a `Content-Length` header to be provided.
 * <p>
 * Consequently, if you are not using chunked HTTP then you must set the `Content-Length` header before writing
 * to the request, as it will be too late otherwise.
 * <p>
 * If you are calling one of the `end` methods that take a string or buffer then Vert.x will automatically calculate
 * and set the `Content-Length` header before writing the request body.
 * <p>
 * If you are using HTTP chunking a a `Content-Length` header is not required, so you do not have to calculate the size
 * up-front.
 * <p>
 * ==== Writing request headers
 * <p>
 * You can write headers to a request using the {@link io.advantageous.conekt.http.HttpClientRequest#headers()} multi-map as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example37}
 * ----
 * <p>
 * The headers are an instance of {@link io.advantageous.conekt.MultiMap} which provides operations for adding, setting and removing
 * entries. Http headers allow more than one value for a specific key.
 * <p>
 * You can also write headers using {@link io.advantageous.conekt.http.HttpClientRequest#putHeader}
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example38}
 * ----
 * <p>
 * If you wish to write headers to the request you must do so before any part of the request body is written.
 * <p>
 * ==== Ending HTTP requests
 * <p>
 * Once you have finished with the HTTP request you must end it with one of the {@link io.advantageous.conekt.http.HttpClientRequest#end}
 * operations.
 * <p>
 * Ending a request causes any headers to be written, if they have not already been written and the request to be marked
 * as complete.
 * <p>
 * Requests can be ended in several ways. With no arguments the request is simply ended:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example39}
 * ----
 * <p>
 * Or a string or buffer can be provided in the call to `end`. This is like calling `write` with the string or buffer
 * before calling `end` with no arguments
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example40}
 * ----
 * <p>
 * ==== Chunked HTTP requests
 * <p>
 * Vert.x supports http://en.wikipedia.org/wiki/Chunked_transfer_encoding[HTTP Chunked Transfer Encoding] for requests.
 * <p>
 * This allows the HTTP request body to be written in chunks, and is normally used when a large request body is being streamed
 * to the server, whose size is not known in advance.
 * <p>
 * You put the HTTP request into chunked mode using {@link io.advantageous.conekt.http.HttpClientRequest#setChunked(boolean)}.
 * <p>
 * In chunked mode each call to write will cause a new chunk to be written to the wire. In chunked mode there is
 * no need to set the `Content-Length` of the request up-front.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example41}
 * ----
 * <p>
 * ==== Request timeouts
 * <p>
 * You can set a timeout for a specific http request using {@link io.advantageous.conekt.http.HttpClientRequest#setTimeout(long)}.
 * <p>
 * If the request does not return any data within the timeout period an exception will be passed to the exception handler
 * (if provided) and the request will be closed.
 * <p>
 * ==== Handling exceptions
 * <p>
 * You can handle exceptions corresponding to a request by setting an exception handler on the
 * {@link io.advantageous.conekt.http.HttpClientRequest} instance:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example42}
 * ----
 * <p>
 * This does not handle non _2xx_ response that need to be handled in the
 * {@link io.advantageous.conekt.http.HttpClientResponse} code:
 * <p>
 * [source, $lang]
 * ----
 * {@link examples.HTTPExamples#statusCodeHandling}
 * ----
 * <p>
 * IMPORTANT: `XXXNow` methods cannot receive an exception handler.
 * <p>
 * ==== Specifying a handler on the client request
 * <p>
 * Instead of providing a response handler in the call to create the client request object, alternatively, you can
 * not provide a handler when the request is created and set it later on the request object itself, using
 * {@link io.advantageous.conekt.http.HttpClientRequest#handler(Handler)}, for example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example43}
 * ----
 * <p>
 * ==== Using the request as a stream
 * <p>
 * The {@link io.advantageous.conekt.http.HttpClientRequest} instance is also a {@link io.advantageous.conekt.streams.WriteStream} which means
 * you can pump to it from any {@link io.advantageous.conekt.streams.ReadStream} instance.
 * <p>
 * For, example, you could pump a file on disk to a http request body as follows:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example44}
 * ----
 * <p>
 * === Handling http responses
 * <p>
 * You receive an instance of {@link io.advantageous.conekt.http.HttpClientResponse} into the handler that you specify in of
 * the request methods or by setting a handler directly on the {@link io.advantageous.conekt.http.HttpClientRequest} object.
 * <p>
 * You can query the status code and the status message of the response with {@link io.advantageous.conekt.http.HttpClientResponse#statusCode}
 * and {@link io.advantageous.conekt.http.HttpClientResponse#statusMessage}.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example45}
 * ----
 * <p>
 * ==== Using the response as a stream
 * <p>
 * The {@link io.advantageous.conekt.http.HttpClientResponse} instance is also a {@link io.advantageous.conekt.streams.ReadStream} which means
 * you can pump it to any {@link io.advantageous.conekt.streams.WriteStream} instance.
 * <p>
 * ==== Response headers and trailers
 * <p>
 * Http responses can contain headers. Use {@link io.advantageous.conekt.http.HttpClientResponse#headers} to get the headers.
 * <p>
 * The object returned is a {@link io.advantageous.conekt.MultiMap} as HTTP headers can contain multiple values for single keys.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example46}
 * ----
 * <p>
 * Chunked HTTP responses can also contain trailers - these are sent in the last chunk of the response body.
 * <p>
 * You use {@link io.advantageous.conekt.http.HttpClientResponse#trailers} to get the trailers. Trailers are also a {@link io.advantageous.conekt.MultiMap}.
 * <p>
 * ==== Reading the request body
 * <p>
 * The response handler is called when the headers of the response have been read from the wire.
 * <p>
 * If the response has a body this might arrive in several pieces some time after the headers have been read. We
 * don't wait for all the body to arrive before calling the response handler as the response could be very large and we
 * might be waiting a long time, or run out of memory for large responses.
 * <p>
 * As parts of the response body arrive, the {@link io.advantageous.conekt.http.HttpClientResponse#handler} is called with
 * a {@link io.advantageous.conekt.buffer.Buffer} representing the piece of the body:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example47}
 * ----
 * <p>
 * If you know the response body is not very large and want to aggregate it all in memory before handling it, you can
 * either aggregate it yourself:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example48}
 * ----
 * <p>
 * Or you can use the convenience {@link io.advantageous.conekt.http.HttpClientResponse#bodyHandler(Handler)} which
 * is called with the entire body when the response has been fully read:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example49}
 * ----
 * <p>
 * ==== Response end handler
 * <p>
 * The response {@link io.advantageous.conekt.http.HttpClientResponse#endHandler} is called when the entire response body has been read
 * or immediately after the headers have been read and the response handler has been called if there is no body.
 * <p>
 * ==== Reading cookies from the response
 * <p>
 * You can retrieve the list of cookies from a response using {@link io.advantageous.conekt.http.HttpClientResponse#cookies()}.
 * <p>
 * Alternatively you can just parse the `Set-Cookie` headers yourself in the response.
 * <p>
 * <p>
 * ==== 100-Continue handling
 * <p>
 * According to the http://www.w3.org/Protocols/rfc2616/rfc2616-sec8.html[HTTP 1.1 specification] a client can set a
 * header `Expect: 100-Continue` and send the request header before sending the rest of the request body.
 * <p>
 * The server can then respond with an interim response status `Status: 100 (Continue)` to signify to the client that
 * it is ok to send the rest of the body.
 * <p>
 * The idea here is it allows the server to authorise and accept/reject the request before large amounts of data are sent.
 * Sending large amounts of data if the request might not be accepted is a waste of bandwidth and ties up the server
 * in reading data that it will just discard.
 * <p>
 * Vert.x allows you to set a {@link io.advantageous.conekt.http.HttpClientRequest#continueHandler(Handler)} on the
 * client request object
 * <p>
 * This will be called if the server sends back a `Status: 100 (Continue)` response to signify that it is ok to send
 * the rest of the request.
 * <p>
 * This is used in conjunction with {@link io.advantageous.conekt.http.HttpClientRequest#sendHead()}to send the head of the request.
 * <p>
 * Here's an example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50}
 * ----
 * <p>
 * On the server side a Vert.x http server can be configured to automatically send back 100 Continue interim responses
 * when it receives an `Expect: 100-Continue` header.
 * <p>
 * This is done by setting the option {@link io.advantageous.conekt.http.HttpServerOptions#setHandle100ContinueAutomatically(boolean)}.
 * <p>
 * If you'd prefer to decide whether to send back continue responses manually, then this property should be set to
 * `false` (the default), then you can inspect the headers and call {@link io.advantageous.conekt.http.HttpServerResponse#writeContinue()}
 * to have the client continue sending the body:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50_1}
 * ----
 * <p>
 * You can also reject the request by sending back a failure status code directly: in this case the body
 * should either be ignored or the connection should be closed (100-Continue is a performance hint and
 * cannot be a logical protocol constraint):
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50_2}
 * ----
 * <p>
 * === Enabling compression on the client
 * <p>
 * The http client comes with support for HTTP Compression out of the box.
 * <p>
 * This means the client can let the remote http server know that it supports compression, and will be able to handle
 * compressed response bodies.
 * <p>
 * An http server is free to either compress with one of the supported compression algorithms or to send the body back
 * without compressing it at all. So this is only a hint for the Http server which it may ignore at will.
 * <p>
 * To tell the http server which compression is supported by the client it will include an `Accept-Encoding` header with
 * the supported compression algorithm as value. Multiple compression algorithms are supported. In case of Vert.x this
 * will result in the following header added:
 * <p>
 * Accept-Encoding: gzip, deflate
 * <p>
 * The server will choose then from one of these. You can detect if a server ompressed the body by checking for the
 * `Content-Encoding` header in the response sent back from it.
 * <p>
 * If the body of the response was compressed via gzip it will include for example the following header:
 * <p>
 * Content-Encoding: gzip
 * <p>
 * To enable compression set {@link io.advantageous.conekt.http.HttpClientOptions#setTryUseCompression(boolean)} on the options
 * used when creating the client.
 * <p>
 * By default compression is disabled.
 * <p>
 * === Pooling and keep alive
 * <p>
 * Http keep alive allows http connections to be used for more than one request. This can be a more efficient use of
 * connections when you're making multiple requests to the same server.
 * <p>
 * The http client supports pooling of connections, allowing you to reuse connections between requests.
 * <p>
 * For pooling to work, keep alive must be true using {@link io.advantageous.conekt.http.HttpClientOptions#setKeepAlive(boolean)}
 * on the options used when configuring the client. The default value is true.
 * <p>
 * When keep alive is enabled. Vert.x will add a `Connection: Keep-Alive` header to each HTTP/1.0 request sent.
 * When keep alive is disabled. Vert.x will add a `Connection: Close` header to each HTTP/1.1 request sent to signal
 * that the connection will be closed after completion of the response.
 * <p>
 * The maximum number of connections to pool *for each server* is configured using {@link io.advantageous.conekt.http.HttpClientOptions#setMaxPoolSize(int)}
 * <p>
 * When making a request with pooling enabled, Vert.x will create a new connection if there are less than the maximum number of
 * connections already created for that server, otherwise it will add the request to a queue.
 * <p>
 * Keep alive connections will not be closed by the client automatically. To close them you can close the client instance.
 * <p>
 * Alternatively you can set idle timeout using {@link io.advantageous.conekt.http.HttpClientOptions#setIdleTimeout(int)} - any
 * connections not used within this timeout will be closed. Please note the idle timeout value is in seconds not milliseconds.
 * <p>
 * === Pipe-lining
 * <p>
 * The client also supports pipe-lining of requests on a connection.
 * <p>
 * Pipe-lining means another request is sent on the same connection before the response from the preceding one has
 * returned. Pipe-lining is not appropriate for all requests.
 * <p>
 * To enable pipe-lining, it must be enabled using {@link io.advantageous.conekt.http.HttpClientOptions#setPipelining(boolean)}.
 * By default pipe-lining is disabled.
 * <p>
 * When pipe-lining is enabled requests will be written to connections without waiting for previous responses to return.
 * <p>
 * === HttpClient usage
 * <p>
 * The HttpClient can be used in a IoActor or embedded.
 * <p>
 * When used in a IoActor, the IoActor *should use its own client instance*.
 * <p>
 * More generally a client should not be shared between different Vert.x contexts as it can lead to unexpected behavior.
 * <p>
 * For example a keep-alive connection will call the client handlers on the context of the request that opened the connection, subsequent requests will use
 * the same context.
 * <p>
 * When this happen Vert.x detects it and log a warn:
 * <p>
 * ----
 * Reusing a connection with a different context: an HttpClient is probably shared between different Verticles
 * ----
 * <p>
 * The HttpClient can be embedded in a non Vert.x thread like a unit test or a plain java `main`: the client handlers
 * will be called by different Vert.x threads and contexts, such contexts are created as needed. For production this
 * usage is not recommended.
 * <p>
 * === Server sharing
 * <p>
 * When several HTTP servers listen on the same port, vert.x orchestrates the request handling using a
 * round-robin strategy.
 * <p>
 * Let's take a verticle creating a HTTP server such as:
 * <p>
 * .io.vertx.examples.http.sharing.HttpServerVerticle
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#serversharing(Conekt)}
 * ----
 * <p>
 * This service is listening on the port 8080. So, when this verticle is instantiated multiple times as with:
 * `conekt run io.conekt.examples.http.sharing.HttpServerVerticle -instances 2`, what's happening ? If both
 * verticles would bind to the same port, you would receive a socket exception. Fortunately, vert.x is handling
 * this case for you. When you deploy another server on the same host and port as an existing server it doesn't
 * actually try and create a new server listening on the same host/port. It binds only once to the socket. When
 * receiving a request it calls the server handlers following a round robin strategy.
 * <p>
 * Let's now imagine a client such as:
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#serversharingclient(Conekt)}
 * ----
 * <p>
 * Vert.x delegates the requests to one of the server sequentially:
 * <p>
 * [source]
 * ----
 * Hello from i.v.e.h.s.HttpServerVerticle@1
 * Hello from i.v.e.h.s.HttpServerVerticle@2
 * Hello from i.v.e.h.s.HttpServerVerticle@1
 * Hello from i.v.e.h.s.HttpServerVerticle@2
 * ...
 * ----
 * <p>
 * Consequently the servers can scale over available cores while each Vert.x verticle instance remains strictly
 * single threaded, and you don't have to do any special tricks like writing load-balancers in order to scale your
 * server on your multi-core machine.
 * <p>
 * === Using HTTPS with Vert.x
 * <p>
 * Vert.x http servers and clients can be configured to use HTTPS in exactly the same way as net servers.
 * <p>
 * Please see <<ssl, configuring net servers to use SSL>> for more information.
 * <p>
 * === WebSockets
 * <p>
 * http://en.wikipedia.org/wiki/WebSocket[WebSockets] are a web technology that allows a full duplex socket-like
 * connection between HTTP servers and HTTP clients (typically browsers).
 * <p>
 * Vert.x supports WebSockets on both the client and server-side.
 * <p>
 * ==== WebSockets on the server
 * <p>
 * There are two ways of handling WebSockets on the server side.
 * <p>
 * ===== WebSocket handler
 * <p>
 * The first way involves providing a {@link io.advantageous.conekt.http.HttpServer#websocketHandler(Handler)}
 * on the server instance.
 * <p>
 * When a WebSocket connection is made to the server, the handler will be called, passing in an instance of
 * {@link io.advantageous.conekt.http.ServerWebSocket}.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example51}
 * ----
 * <p>
 * You can choose to reject the WebSocket by calling {@link io.advantageous.conekt.http.ServerWebSocket#reject()}.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example52}
 * ----
 * <p>
 * ===== Upgrading to WebSocket
 * <p>
 * The second way of handling WebSockets is to handle the HTTP Upgrade request that was sent from the client, and
 * call {@link io.advantageous.conekt.http.HttpServerRequest#upgrade()} on the server request.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example53}
 * ----
 * <p>
 * ===== The server WebSocket
 * <p>
 * The {@link io.advantageous.conekt.http.ServerWebSocket} instance enables you to retrieve the {@link io.advantageous.conekt.http.ServerWebSocket#headers() headers},
 * {@link io.advantageous.conekt.http.ServerWebSocket#path() path}, {@link io.advantageous.conekt.http.ServerWebSocket#query() query} and
 * {@link io.advantageous.conekt.http.ServerWebSocket#uri() URI} of the HTTP request of the WebSocket handshake.
 * <p>
 * ==== WebSockets on the client
 * <p>
 * The Vert.x {@link io.advantageous.conekt.http.HttpClient} supports WebSockets.
 * <p>
 * You can connect a WebSocket to a server using one of the {@link io.advantageous.conekt.http.HttpClient#websocket} operations and
 * providing a handler.
 * <p>
 * The handler will be called with an instance of {@link io.advantageous.conekt.http.WebSocket} when the connection has been made:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example54}
 * ----
 * <p>
 * ==== Writing messages to WebSockets
 * <p>
 * If you wish to write a single binary WebSocket message to the WebSocket you can do this with
 * {@link io.advantageous.conekt.http.WebSocket#writeBinaryMessage(Buffer)}:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example55}
 * ----
 * <p>
 * If the WebSocket message is larger than the maximum websocket frame size as configured with
 * {@link io.advantageous.conekt.http.HttpClientOptions#setMaxWebsocketFrameSize(int)}
 * then Vert.x will split it into multiple WebSocket frames before sending it on the wire.
 * <p>
 * ==== Writing frames to WebSockets
 * <p>
 * A WebSocket message can be composed of multiple frames. In this case the first frame is either a _binary_ or _text_ frame
 * followed by zero or more _continuation_ frames.
 * <p>
 * The last frame in the message is marked as _final_.
 * <p>
 * To send a message consisting of multiple frames you create frames using
 * {@link io.advantageous.conekt.http.WebSocketFrame#binaryFrame(Buffer, boolean)}
 * , {@link io.advantageous.conekt.http.WebSocketFrame#textFrame(java.lang.String, boolean)} or
 * {@link io.advantageous.conekt.http.WebSocketFrame#continuationFrame(Buffer, boolean)} and write them
 * to the WebSocket using {@link io.advantageous.conekt.http.WebSocket#writeFrame(WebSocketFrame)}.
 * <p>
 * Here's an example for binary frames:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example56}
 * ----
 * <p>
 * In many cases you just want to send a websocket message that consists of a single final frame, so we provide a couple
 * of shortcut methods to do that with {@link io.advantageous.conekt.http.WebSocket#writeFinalBinaryFrame(Buffer)}
 * and {@link io.advantageous.conekt.http.WebSocket#writeFinalTextFrame(String)}.
 * <p>
 * Here's an example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example56_1}
 * ----
 * <p>
 * ==== Reading frames from WebSockets
 * <p>
 * To read frames from a WebSocket you use the {@link io.advantageous.conekt.http.WebSocket#frameHandler(Handler)}.
 * <p>
 * The frame handler will be called with instances of {@link io.advantageous.conekt.http.WebSocketFrame} when a frame arrives,
 * for example:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example57}
 * ----
 * <p>
 * ==== Closing WebSockets
 * <p>
 * Use {@link io.advantageous.conekt.http.WebSocket#close()} to close the WebSocket connection when you have finished with it.
 * <p>
 * ==== Streaming WebSockets
 * <p>
 * The {@link io.advantageous.conekt.http.WebSocket} instance is also a {@link io.advantageous.conekt.streams.ReadStream} and a
 * {@link io.advantageous.conekt.streams.WriteStream} so it can be used with pumps.
 * <p>
 * When using a WebSocket as a write stream or a read stream it can only be used with WebSockets connections that are
 * used with binary frames that are no split over multiple frames.
 * <p>
 * === Automatic clean-up in verticles
 * <p>
 * If you're creating http servers and clients from inside verticles, those servers and clients will be automatically closed
 * when the verticle is undeployed.
 */
package io.advantageous.conekt.http;

import io.advantageous.conekt.Conekt;
import io.advantageous.conekt.Handler;
import io.advantageous.conekt.buffer.Buffer;

