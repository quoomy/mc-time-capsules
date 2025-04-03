import fastify from 'fastify';

const app = fastify();

async function startServer() {
	app.ready();
	try {
		await app.listen({ port: 4242, host: '0.0.0.0' });
	} catch (err) {
		app.log.error(err);
		process.exit(1);
	}
}
startServer();

app.get('/', async (request, reply) => {
	return { hello: 'world' };
});
