const glob = require('@actions/glob');

async function foo() {
    const patterns = ['**/target/surefire-reports/TEST-*.xml']
    const globber = await glob.create(patterns.join('\n'))
    const files = await globber.glob()
    console.log(files)
    console.log(files.length)
}
foo()
