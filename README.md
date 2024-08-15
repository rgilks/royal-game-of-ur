# Royal Game of Ur - ClojureScript Implementation

This project is a modern implementation of the ancient Royal Game of Ur using ClojureScript, React, and AWS Amplify. It features a web-based interface with real-time multiplayer functionality.

## Features

- ClojureScript frontend using Pitch UIx library
- State management with com.fbeyer/refx
- AWS Amplify for hosting and authentication
- Backend game logic running in AWS Lambda
- Real-time game state synchronization using AWS AppSync and DynamoDB
- Material-UI for styling

## Prerequisites

- Node.js (>=21.0.0)
- npm (>=8.0.0)
- AWS account with Amplify CLI configured
- Clojure/ClojureScript development environment

## Setup

1. Clone the repository:
   ```
   git clone [your-repo-url]
   cd royal-game-of-ur
   ```

2. Install dependencies:
   ```
   yarn install
   ```

3. Initialize Amplify (if not already done):
   ```
   amplify init
   ```

4. Push the Amplify backend:
   ```
   amplify push
   ```

5. Start the development server:
   ```
   yarn watch
   ```

6. In a separate terminal, start webpack:
   ```
   yarn webpack
   ```

7. (Optional) Start the Karma test runner:
   ```
   yarn karma
   ```

## Project Structure

- `src/amplify`: Main source directory for ClojureScript code
- `public`: Static assets and HTML template
- `amplify`: AWS Amplify configuration and backend resources

## Available Scripts

- `yarn watch`: Start the ClojureScript compiler in watch mode
- `yarn webpack`: Start webpack in watch mode
- `yarn karma`: Run tests using Karma
- `yarn build`: Build the project for production
- `yarn cypress`: Run Cypress tests (requires additional setup)

## Deployment

The project is set up for continuous deployment with AWS Amplify. Push to the main branch to trigger a deployment.

## Testing

- Unit tests: Run `yarn karma`
- E2E tests: Run `yarn cypress` (requires additional setup as described in the original template README)

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- This project was bootstrapped using the [AWS Amplify ClojureScript Template](https://github.com/rgilks/aws-amplify-cljs-template)
