import fastify from 'fastify';
import fastifyView from '@fastify/view';
import ejs from 'ejs';
import path from 'node:path';

const app = fastify();

app.register(fastifyView, {
	engine: {
		ejs,
	},
	root: path.join(__dirname, '../assets/layouts'),
	options: {
		context: {
			get: (obj: Record<string, unknown>, prop: string) => obj?.[prop],
		},
	},
	viewExt: 'ejs',
});

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
	return reply.view('index.ejs', {
		customMsg: 'Fastify EJS Testing 123',
	});
});
