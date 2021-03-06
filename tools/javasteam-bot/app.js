const request = require('request');
const octokit = require('@octokit/rest')();
const argv = require('yargs').argv;
const jwt = require('jsonwebtoken');
const fs = require('fs');

if (!argv.appid) {
    console.log("Missing appid argument");
    process.exit();
}

if (!argv.key) {
    console.log("Missing key argument");
    process.exit();
}

if (!argv.installid) {
    console.log("Missing installid argument");
    process.exit();
}

let latestPullRequestFile = "latest_pull_request.txt";
let latestPullRequest = 0;

try {
    latestPullRequest = parseInt(fs.readFileSync(latestPullRequestFile));
} catch (ignored) {
}

const repo = argv.repo ? argv.repo : 'labrat';
const owner = argv.owner ? argv.owner : 'Longi94';

const cert = fs.readFileSync(argv.key);

async function authenticateOcto(octokit) {
    console.log("Authenticating octokit");

    console.log("Generating JWT payload");
    // Generate the JWT
    const payload = {
        // issued at time
        iat: Math.floor(Date.now() / 1000),
        //JWT expiration time (10 minute maximum)
        exp: Math.floor(Date.now() / 1000) + (9 * 60),
        //GitHub App's identifier
        iss: argv.appid
    };

    console.log("Signing jwt payload");
    const token = jwt.sign(payload, cert, {algorithm: 'RS256'});

    console.log("Authenticating with the generated token");
    octokit.authenticate({
        type: 'app',
        token: token
    });

    console.log("Creating installation token");
    const result = await octokit.apps.createInstallationToken({installation_id: argv.installid});

    console.log("Authenticating with installation token");
    octokit.authenticate({type: 'token', token: result.data.token});
}

const steamKitOptions = {
    url: 'https://api.github.com/repos/SteamRE/SteamKit/pulls?state=closed',
    headers: {
        'User-Agent': 'javasteam-bot'
    }
};

// run once at start up
checkPullRequests(octokit);

function checkPullRequests(github) {
    console.log('Checking for new merged PRs...');

    const steamKitPromise = new Promise((resolve, reject) => {
        request(steamKitOptions, (error, response, body) => {
            if (error) {
                console.log(error);
                return reject(error);
            }

            resolve(body);
        });
    });

    steamKitPromise.then(response => {
        const pullRequests = JSON.parse(response);

        authenticateOcto(octokit).then(() => {
            const javaSteamPromise = github.issues.getForRepo({
                owner: owner,
                repo: repo,
                state: 'all',
                labels: 'MPR',
                filter: 'created'
            });

            javaSteamPromise.then(issues => {
                issues = issues.data;

                const latest = issues.length > 0 ? new Date(issues[0].created_at).getTime() : 0;

                console.log('Newest MPR issue ' + latest);

                const newPRs = pullRequests.filter(pr => {
                    const mergeTime = new Date(pr.merged_at).getTime();

                    return pr.merged_at && latestPullRequest < mergeTime && latest < mergeTime;
                });

                console.log(newPRs.length + ' new pull requests to process.');

                if (newPRs.length > 0) {
                    newPRs.forEach(pr => {
                        if (pr.merged_at) {

                            const mergeTime = new Date(pr.merged_at).getTime();

                            createIssue(octokit, pr);

                            if (latestPullRequest < mergeTime) {
                                latestPullRequest = mergeTime;
                            }
                        }
                    });
                }

                fs.writeFileSync(latestPullRequestFile, latestPullRequest.toString());
            }, console.log);
        }, console.log);
    }, console.log);
}

function createIssue(octokit, pullRequest) {
    console.log('Creating issue for ' + pullRequest.number);

    octokit.issues.create({
        owner: owner,
        repo: repo,
        title: 'MPR-' + pullRequest.number + ' ' + pullRequest.title,
        body: 'A new PR was merged over at [SteamKit](https://github.com/SteamRE/SteamKit). It should be checked if it\'s relevant for JavaSteam or not.\n' +
        '\n' +
        'Merged at ' + pullRequest.merged_at + '\n' +
        '[Head over to the PRs to see more details.](https://github.com/SteamRE/SteamKit/pulls?q=is%3Apr+is%3Aclosed) (no direct links to avoid reference spamming).',
        labels: ['MPR']
    }).then(() => {
        console.log('Created issue for' + pullRequest.number);
    }, reason => {
        console.log('Failed to create issue for' + pullRequest.number);
        console.log(reason);
    });
}
