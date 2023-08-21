# Implementation Details

## Connecting to Inspector Axon
This connection is configured in resources/application.conf. Because I didn't want to commit credentials, I have
that file look up environment variables. You can set them in your IDE/shell, or add them as java system properties:
```
-DIA_WORKSPACE=your-inspector-axon-workspace -DIA_ENVIRONMENT=your-inspector-axon-environment -DIA_TOKEN=your-inspector-axon-token
```
