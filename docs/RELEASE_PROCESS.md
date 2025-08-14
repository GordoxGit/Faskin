# Release Process

This guide is for maintainers creating a production release of HeneriaCore.

## Preparation

1. Ensure `CHANGELOG.md` and version numbers are updated.
2. Commit and push all changes to `main` or `develop`.
3. Verify CI is green and run the manual tests in `RELEASE_CHECKLIST.md`.

## Trigger the `release-with-plib` Workflow

The workflow bundles ProtocolLib into the plugin and creates a GitHub release. It is triggered manually using **workflow_dispatch**.

Inputs:
- `protocollib_source` – one of `github-packages`, `s3`, or `manual-upload`.
- `protocollib_ref` – tag, S3 key or URL depending on the source.
- `version` – optional override; defaults to `gradle.properties` value.
- `release_notes` – optional text appended to the GitHub release.

## Providing ProtocolLib

### GitHub Packages (recommended)
1. Upload `ProtocolLib.jar` to a private repository release or package.
2. Store a token with `read:packages` permission in `GH_PACKAGES_TOKEN`.
3. Dispatch the workflow with `protocollib_source=github-packages` and set `protocollib_ref` to the asset tag or ID.
4. The job downloads the jar using `curl -H "Authorization: token $GH_PACKAGES_TOKEN"`.

### S3
1. Upload the jar to a private S3 bucket.
2. Store `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `S3_BUCKET` secrets.
3. Dispatch with `protocollib_source=s3` and `protocollib_ref` set to the object key.
4. The job runs `aws s3 cp s3://$S3_BUCKET/$protocollib_ref ./libs/ProtocolLib.jar`.

### Manual Upload
1. Generate a short‑lived, pre‑signed URL to the jar.
2. Store it as the secret `PROTOCOLLIB_UPLOAD_URL` or pass via `protocollib_ref`.
3. Dispatch with `protocollib_source=manual-upload`.
4. The job downloads with `curl -L "$PROTOCOLLIB_UPLOAD_URL" -o ./libs/ProtocolLib.jar`.

## Build and Release

1. The workflow validates the jar, then runs:
   ```bash
   ./gradlew -PwithPlib=true -PwithPlibLocal=./libs/ProtocolLib.jar clean shadowJar test
   ```
2. The artifact `HeneriaCore-withPlib-<version>.jar` and its SHA256 checksum are produced in `build/libs/`.
3. A draft GitHub release `v<version>` is created and both files are uploaded.
4. Review the release, add final notes, and publish.

## Post Release

- Announce in the server admin channel.
- Upload the artifact to internal mirrors if required.
- Plan rollback: keep previous release jar handy in case of issues.
