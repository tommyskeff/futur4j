const PACKAGE = "src/main/java/dev/tommyjs/futur"
const SIGNAL = "// Generated delegates to static factory"

const regex = /( {4}\/\*\*.+?)?((?:\S| )+? )(\S+)(\(.*?\))(?: {.+?}|;)/gs
const content = await Bun.file(`../../futur-api/${PACKAGE}/promise/PromiseFactory.java`).text()

const methods = [""]
for (const match of content.matchAll(regex)) {
	let [_, docs, head, name, params] = match

	head = head.trimStart()
	if (head.startsWith("static")) continue
	if (head.startsWith("default")) head = head.slice(8);

	const args = Array.from(params.matchAll(/ ([a-zA-Z1-9]+)[,)]/gs))
		.map(v => v[1]).join(", ")

	methods.push(
		[
			`${docs}    public static ${head}${name}${params} {`,
			`        return factory.${name}(${args});`,
			"    }"
		].join("\n")
	)
}

const output = Bun.file(`../${PACKAGE}/lazy/Promises.java`)

const existing = await output.text()
const cutIndex = existing.indexOf(SIGNAL) + SIGNAL.length;
const newContent = existing.slice(0, cutIndex) + methods.join("\n\n") + "\n\n}"

await Bun.write(output, newContent)